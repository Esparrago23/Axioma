package com.patatus.axioma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.features.auth.domain.usecases.LoginUseCase
import com.patatus.axioma.features.auth.presentation.screens.LoginScreen
import com.patatus.axioma.features.auth.presentation.viewmodels.LoginViewModel
import com.patatus.axioma.features.auth.presentation.viewmodels.LoginViewModelFactory
import com.patatus.axioma.ui.theme.AppTheme

import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = AppContainer(applicationContext)

        setContent {
            AppTheme {
                val loginUseCase = LoginUseCase(appContainer.authRepository)
                val factory = LoginViewModelFactory(loginUseCase)

                val viewModel: LoginViewModel = viewModel(factory = factory)

                LoginScreen(
                    viewModel = viewModel,
                    onNavigateHome = {

                        Toast.makeText(
                            applicationContext,
                            "¡Bienvenido!",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                )
            }
        }
    }
}