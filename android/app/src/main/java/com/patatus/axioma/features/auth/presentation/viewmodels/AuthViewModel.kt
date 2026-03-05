package com.patatus.axioma.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.auth.domain.usecases.LoginUseCase
import com.patatus.axioma.features.auth.domain.usecases.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

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

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object SuccessRegister : AuthUiState()
    data class SuccessLogin(val username: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}