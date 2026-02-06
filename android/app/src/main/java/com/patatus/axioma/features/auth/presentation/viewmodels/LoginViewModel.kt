package com.patatus.axioma.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.auth.domain.usecases.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(value: String) { _email.value = value }
    fun onPasswordChanged(value: String) { _password.value = value }

    fun onLogin() {
        if (_email.value.isBlank() || _password.value.isBlank()) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(_email.value, _password.value)

            result.onSuccess { user ->
                _uiState.value = LoginUiState.Success(user.username)
            }.onFailure { error ->
                _uiState.value = LoginUiState.Error(error.message ?: "Error desconocido")
            }
        }
    }
}

// Un estado sellado para manejar mejor la UI
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val username: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}