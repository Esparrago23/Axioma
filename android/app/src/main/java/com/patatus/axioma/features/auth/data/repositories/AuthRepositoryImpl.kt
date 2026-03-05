package com.patatus.axioma.features.auth.data.repositories

import com.patatus.axioma.core.network.TokenManager
import com.patatus.axioma.features.auth.data.datasources.remote.api.AuthApiService
import com.patatus.axioma.features.auth.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.auth.data.datasources.remote.models.LoginRequest
import com.patatus.axioma.features.auth.domain.entities.User
import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, pass))
                TokenManager.saveToken(response.accessToken)
                Result.success(response.toDomain())
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody).getString("detail")
                } catch (jsonException: Exception) {
                    "Error desconocido en el servidor (${e.code()})"
                }
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Verifica tu internet."))
            }
        }
    }

    override suspend fun register(email: String, pass: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.register(LoginRequest(email, pass))
                Result.success(true)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody).getString("detail")
                } catch (jsonException: Exception) {
                    "Error desconocido en el servidor"
                }
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Revisa tu internet."))
            }
        }
    }
}