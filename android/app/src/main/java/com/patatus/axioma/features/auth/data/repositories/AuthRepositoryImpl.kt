package com.patatus.axioma.features.auth.data.repositories

import com.patatus.axioma.core.network.TokenManager
import com.patatus.axioma.core.network.SecureSessionStore
import com.patatus.axioma.features.auth.data.datasources.remote.api.AuthApiService
import com.patatus.axioma.features.auth.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.auth.data.datasources.remote.models.LoginRequest
import com.patatus.axioma.features.auth.data.datasources.remote.models.LogoutRequest
import com.patatus.axioma.features.auth.data.datasources.remote.models.RefreshTokenRequest
import com.patatus.axioma.features.auth.domain.entities.User
import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val secureSessionStore: SecureSessionStore,
    private val usersRepository: UsersRepository
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, pass))
                TokenManager.saveToken(response.accessToken)
                secureSessionStore.saveRefreshToken(response.refreshToken)
                Result.success(response.toDomain())
            } catch (e: HttpException) {
                Result.failure(Exception(parseApiError(e)))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Verifica tu internet."))
            }
        }
    }

    override suspend fun quickLoginWithStoredSession(): Result<User> {
        return withContext(Dispatchers.IO) {
            val storedRefreshToken = secureSessionStore.getRefreshToken()
            if (storedRefreshToken.isNullOrBlank()) {
                return@withContext Result.failure(Exception("No hay sesion guardada para inicio rapido"))
            }

            try {
                val response = apiService.refresh(
                    RefreshTokenRequest(
                        refreshToken = storedRefreshToken,
                        deviceName = "android-biometric",
                    )
                )

                TokenManager.saveToken(response.accessToken)
                secureSessionStore.saveRefreshToken(response.refreshToken)
                Result.success(response.toDomain())
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    secureSessionStore.clearRefreshToken()
                    TokenManager.clearToken()
                    return@withContext Result.failure(Exception("Tu sesion rapida expiro. Inicia sesion de nuevo."))
                }
                Result.failure(Exception(parseApiError(e)))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexion. Verifica tu internet."))
            }
        }
    }

    override fun hasStoredQuickSession(): Boolean {
        return !secureSessionStore.getRefreshToken().isNullOrBlank()
    }

    override suspend fun clearStoredSession() {
        withContext(Dispatchers.IO) {
            // Best effort: desregistrar token push antes de invalidar sesion local/remota.
            runCatching {
                usersRepository.updatePushRegistration(
                    fcmToken = null,
                    lastLatitude = null,
                    lastLongitude = null,
                    forceNullTokenField = true
                )
            }

            val storedRefreshToken = secureSessionStore.getRefreshToken()
            if (!storedRefreshToken.isNullOrBlank()) {
                runCatching {
                    apiService.logout(LogoutRequest(storedRefreshToken))
                }
            }
            secureSessionStore.clearRefreshToken()
            TokenManager.clearToken()
        }
    }

    override suspend fun register(email: String, pass: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.register(LoginRequest(email, pass))
                Result.success(true)
            } catch (e: HttpException) {
                Result.failure(Exception(parseApiError(e)))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión. Revisa tu internet."))
            }
        }
    }

    private fun parseApiError(e: HttpException): String {
        val raw = e.response()?.errorBody()?.string()
        return try {
            JSONObject(raw).getString("detail")
        } catch (_: Exception) {
            "Error del servidor (${e.code()})"
        }
    }
}