package com.patatus.axioma.features.auth.presentation.screens

data class AuthState(
    val email: String = "",
    val password: String = "",
    val biometricAvailable: Boolean = false,
    val quickLoginAvailable: Boolean = false,
    val status: AuthStatus = AuthStatus.Idle
)


sealed class AuthStatus {
    object Idle : AuthStatus()
    object Loading : AuthStatus()
    object SuccessRegister : AuthStatus()
    data class SuccessLogin(val username: String) : AuthStatus()
    data class Error(val message: String) : AuthStatus()
}