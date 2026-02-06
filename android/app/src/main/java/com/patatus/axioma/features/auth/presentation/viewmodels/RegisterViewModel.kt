package com.patatus.axioma.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.auth.presentation.screens.RegisterUiState
import com.patatus.axioma.features.auth.domain.usecases.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun onRegister() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = RegisterUiState.Error("Llena todos los campos, no seas flojo.")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            val result = registerUseCase(_email.value, _password.value)

            result.onSuccess {
                _uiState.value = RegisterUiState.Success
            }.onFailure { error ->
                _uiState.value = RegisterUiState.Error(error.message ?: "Error desconocido al registrar")
            }
        }
    }
}