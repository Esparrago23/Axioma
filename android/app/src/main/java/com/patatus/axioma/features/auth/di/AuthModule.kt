package com.patatus.axioma.features.auth.di

import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.features.auth.domain.usecases.LoginUseCase
import com.patatus.axioma.features.auth.domain.usecases.RegisterUseCase
import com.patatus.axioma.features.auth.presentation.viewmodels.LoginViewModelFactory
import com.patatus.axioma.features.auth.presentation.viewmodels.RegisterViewModelFactory

class AuthModule(private val appContainer: AppContainer) {


    private fun provideLoginUseCase(): LoginUseCase {
        return LoginUseCase(appContainer.authRepository)
    }

    private fun provideRegisterUseCase(): RegisterUseCase {
        return RegisterUseCase(appContainer.authRepository)
    }


    fun provideLoginViewModelFactory(): LoginViewModelFactory {
        return LoginViewModelFactory(provideLoginUseCase())
    }

    fun provideRegisterViewModelFactory(): RegisterViewModelFactory {
        return RegisterViewModelFactory(provideRegisterUseCase())
    }
}