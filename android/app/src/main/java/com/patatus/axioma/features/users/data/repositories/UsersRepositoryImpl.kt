package com.patatus.axioma.features.users.data.repositories

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import com.patatus.axioma.features.users.data.datasources.remote.api.UsersApiService
import com.patatus.axioma.features.users.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.users.data.datasources.remote.models.UserUpdateRequest
import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val apiService: UsersApiService,
    @ApplicationContext private val context: Context
) : UsersRepository {

    override suspend fun getMyProfile(): Result<User> {
        return safeApiCall {
            apiService.getMyProfile().toDomain()
        }
    }

    override suspend fun updateMyProfile(
        username: String?,
        fullName: String?,
        profilePictureUrl: String?
    ): Result<User> {
        return safeApiCall {
            apiService.updateMyProfile(
                UserUpdateRequest(
                    username = username,
                    fullName = fullName,
                    profilePictureUrl = profilePictureUrl
                )
            ).toDomain()
        }
    }

    override suspend fun uploadMyProfilePhoto(localUri: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = localUri.toUri()
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = mimeType.substringAfter('/', "jpg")
                val tempFile = File.createTempFile("profile_upload_", ".${extension}", context.cacheDir)

                context.contentResolver.openInputStream(uri).use { input ->
                    if (input == null) {
                        return@withContext Result.failure(Exception("No se pudo leer el archivo seleccionado"))
                    }
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }

                val body = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("photo", tempFile.name, body)

                val updated = apiService.uploadMyProfilePhoto(part).toDomain()
                tempFile.delete()
                Result.success(updated)
            } catch (e: HttpException) {
                Result.failure(Exception(parseErrorMessage(e)))
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "No se pudo subir la foto"))
            }
        }
    }

    override suspend fun deleteMyAccount(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteMyAccount()
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val message = response.errorBody()?.string().orEmpty()
                    Result.failure(Exception(message.ifBlank { "No se pudo eliminar la cuenta" }))
                }
            } catch (e: HttpException) {
                Result.failure(Exception(parseErrorMessage(e)))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexion. Verifica tu internet."))
            }
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(apiCall())
            } catch (e: HttpException) {
                Result.failure(Exception(parseErrorMessage(e)))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexion. Verifica tu internet."))
            }
        }
    }

    private fun parseErrorMessage(e: HttpException): String {
        val raw = e.response()?.errorBody()?.string()
        return try {
            JSONObject(raw).getString("detail")
        } catch (_: Exception) {
            "Error del servidor (${e.code()})"
        }
    }
}
