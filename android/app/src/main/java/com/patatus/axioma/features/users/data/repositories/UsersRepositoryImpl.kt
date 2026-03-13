package com.patatus.axioma.features.users.data.repositories

import android.content.Context
import androidx.core.net.toUri
import com.patatus.axioma.features.users.data.datasources.remote.api.UsersApiService
import com.patatus.axioma.features.users.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.users.data.datasources.remote.models.UpdateFcmTokenRequest
import com.patatus.axioma.features.users.data.datasources.remote.models.UserUpdateRequest
import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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

    override suspend fun updatePushRegistration(
        fcmToken: String?,
        lastLatitude: Double?,
        lastLongitude: Double?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateMyFcmToken(
                    UpdateFcmTokenRequest(
                        fcmToken = fcmToken,
                        lastLatitude = lastLatitude,
                        lastLongitude = lastLongitude
                    )
                )

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val message = response.errorBody()?.string().orEmpty()
                    Result.failure(Exception(message.ifBlank { "No se pudo actualizar el registro push" }))
                }
            } catch (e: HttpException) {
                Result.failure(Exception(parseErrorMessage(e)))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexion. Verifica tu internet."))
            }
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
                val tempFile = File.createTempFile("profile_upload_", ".jpg", context.cacheDir)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val originalBitmap = android.graphics.BitmapFactory.decodeStream(input)
                        ?: return@withContext Result.failure(Exception("No se pudo leer la imagen"))

                    val maxSize = 1080
                    val ratio = kotlin.math.min(
                        maxSize.toFloat() / originalBitmap.width,
                        maxSize.toFloat() / originalBitmap.height
                    )

                    val scaledBitmap = if (ratio < 1f) {
                        android.graphics.Bitmap.createScaledBitmap(
                            originalBitmap,
                            (originalBitmap.width * ratio).toInt(),
                            (originalBitmap.height * ratio).toInt(),
                            true
                        )
                    } else {
                        originalBitmap
                    }

                    tempFile.outputStream().use { output ->
                        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output)
                    }

                    if (scaledBitmap != originalBitmap) {
                        scaledBitmap.recycle()
                    }
                    originalBitmap.recycle()
                }

                val body = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
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