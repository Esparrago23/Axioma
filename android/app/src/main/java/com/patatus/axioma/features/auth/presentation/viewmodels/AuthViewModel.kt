package com.patatus.axioma.features.auth.presentation.viewmodels

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.core.hardware.biometric.BiometricAuthManager
import com.patatus.axioma.core.hardware.biometric.BiometricAvailability
import com.patatus.axioma.core.hardware.notifications.PushNotificationManager
import com.patatus.axioma.features.auth.domain.usecases.HasQuickSessionUseCase
import com.patatus.axioma.features.auth.domain.usecases.LoginUseCase
import com.patatus.axioma.features.auth.domain.usecases.QuickLoginUseCase
import com.patatus.axioma.features.auth.domain.usecases.RegisterUseCase
import com.patatus.axioma.features.auth.presentation.screens.AuthState
import com.patatus.axioma.features.auth.presentation.screens.AuthStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val quickLoginUseCase: QuickLoginUseCase,
    private val hasQuickSessionUseCase: HasQuickSessionUseCase,
    private val biometricAuthManager: BiometricAuthManager,
    private val pushNotificationManager: PushNotificationManager
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    init {
        _state.update { it.copy(quickLoginAvailable = hasQuickSessionUseCase()) }
    }

    fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value) }
    }

    fun onPasswordChanged(value: String) {
        _state.update { it.copy(password = value) }
    }

    fun checkBiometricAvailability(activity: FragmentActivity) {
        val availability = biometricAuthManager.checkAvailability(activity)
        _state.update {
            it.copy(biometricAvailable = (availability == BiometricAvailability.AVAILABLE))
        }
    }

    fun showBiometricPrompt(activity: FragmentActivity) {
        biometricAuthManager.authenticate(
            activity = activity,
            onSuccess = { onQuickLogin() },
            onError = { message -> onBiometricPromptError(message) }
        )
    }

    fun onLogin() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(status = AuthStatus.Error("Llena todos los campos.")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(status = AuthStatus.Loading) }

            val result = loginUseCase(currentState.email, currentState.password)

            result.onSuccess { user ->
                syncPushRegistration()
                _state.update { it.copy(status = AuthStatus.SuccessLogin(user.username)) }
            }.onFailure { error ->
                _state.update {
                    it.copy(status = AuthStatus.Error(error.message ?: "Error desconocido"))
                }
            }
        }
    }

    fun onRegister() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(status = AuthStatus.Error("Llena todos los campos.")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(status = AuthStatus.Loading) }

            val result = registerUseCase(currentState.email, currentState.password)

            result.onSuccess {
                syncPushRegistration()
                _state.update { it.copy(status = AuthStatus.SuccessRegister) }
            }.onFailure { error ->
                _state.update {
                    it.copy(status = AuthStatus.Error(error.message ?: "Error desconocido al registrar"))
                }
            }
        }
    }

    fun onQuickLogin() {
        val currentState = _state.value
        if (!currentState.biometricAvailable || !currentState.quickLoginAvailable) {
            _state.update { it.copy(status = AuthStatus.Error("El inicio rápido no está disponible en este dispositivo")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(status = AuthStatus.Loading) }

            quickLoginUseCase()
                .onSuccess { user ->
                    syncPushRegistration()
                    _state.update {
                        it.copy(
                            quickLoginAvailable = true,
                            status = AuthStatus.SuccessLogin(user.username)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            quickLoginAvailable = hasQuickSessionUseCase(),
                            status = AuthStatus.Error(error.message ?: "No se pudo iniciar sesión rápida")
                        )
                    }
                }
        }
    }

    fun onBiometricPromptError(message: String) {
        _state.update { it.copy(status = AuthStatus.Error(message)) }
    }

    fun resetState() {
        _state.update {
            it.copy(
                status = AuthStatus.Idle,
                quickLoginAvailable = hasQuickSessionUseCase()
            )
        }
    }

    private fun syncPushRegistration() {
        viewModelScope.launch {
            pushNotificationManager.syncTokenAndCurrentLocation()
        }
    }
}

