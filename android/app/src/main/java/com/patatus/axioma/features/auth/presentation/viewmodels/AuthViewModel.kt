package com.patatus.axioma.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.auth.domain.usecases.HasQuickSessionUseCase
import com.patatus.axioma.features.auth.domain.usecases.LoginUseCase
import com.patatus.axioma.features.auth.domain.usecases.QuickLoginUseCase
import com.patatus.axioma.features.auth.domain.usecases.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val quickLoginUseCase: QuickLoginUseCase,
    private val hasQuickSessionUseCase: HasQuickSessionUseCase,
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _biometricAvailable = MutableStateFlow(false)
    val biometricAvailable = _biometricAvailable.asStateFlow()

    private val _quickLoginAvailable = MutableStateFlow(false)
    val quickLoginAvailable = _quickLoginAvailable.asStateFlow()

    init {
        _quickLoginAvailable.value = hasQuickSessionUseCase()
    }

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun onLogin() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = AuthUiState.Error("Llena todos los campos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = loginUseCase(_email.value, _password.value)

            result.onSuccess { user ->
                _uiState.value = AuthUiState.SuccessLogin(user.username)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Error desconocido al iniciar sesión")
            }
        }
    }

    fun onRegister() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = AuthUiState.Error("Llena todos los campos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = registerUseCase(_email.value, _password.value)

            result.onSuccess {
                _uiState.value = AuthUiState.SuccessRegister
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Error desconocido al registrar")
            }
        }
    }

    fun onBiometricAvailabilityChanged(isAvailable: Boolean) {
        _biometricAvailable.value = isAvailable
    }

    fun onQuickLogin() {
        if (!_biometricAvailable.value || !_quickLoginAvailable.value) {
            _uiState.value = AuthUiState.Error("El inicio rapido no esta disponible en este dispositivo")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            quickLoginUseCase()
                .onSuccess { user ->
                    _quickLoginAvailable.value = true
                    _uiState.value = AuthUiState.SuccessLogin(user.username)
                }
                .onFailure { error ->
                    _quickLoginAvailable.value = hasQuickSessionUseCase()
                    _uiState.value = AuthUiState.Error(
                        error.message ?: "No se pudo iniciar sesion rapida"
                    )
                }
        }
    }

    fun onBiometricPromptError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
        _quickLoginAvailable.value = hasQuickSessionUseCase()
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object SuccessRegister : AuthUiState()
    data class SuccessLogin(val username: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}