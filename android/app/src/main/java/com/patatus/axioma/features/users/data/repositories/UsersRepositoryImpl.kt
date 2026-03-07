package com.patatus.axioma.features.users.data.repositories

import com.patatus.axioma.features.users.data.datasources.remote.api.UsersApiService
import com.patatus.axioma.features.users.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.users.data.datasources.remote.models.UserUpdateRequest
import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val apiService: UsersApiService
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
