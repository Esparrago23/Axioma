package com.patatus.axioma.features.auth.data.repositories

import com.patatus.axioma.core.network.AuthApiService
import com.patatus.axioma.features.auth.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.auth.data.datasources.remote.models.LoginRequest
import com.patatus.axioma.features.auth.domain.entities.User
import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import org.json.JSONObject

class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, pass))
                Result.success(response.toDomain())
            } catch (e: Exception) {
                Result.failure(e)
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