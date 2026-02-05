package com.patatus.axioma.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.patatus.axioma.features.auth.domain.usecases.LoginUseCase

class LoginViewModelFactory(
    private val loginUseCase: LoginUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(loginUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}