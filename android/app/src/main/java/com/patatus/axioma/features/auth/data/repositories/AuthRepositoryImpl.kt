package com.patatus.axioma.features.auth.data.repositories

import com.patatus.axioma.core.network.AuthApiService
import com.patatus.axioma.features.auth.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.auth.data.datasources.remote.models.LoginRequest
import com.patatus.axioma.features.auth.domain.entities.User
import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
}