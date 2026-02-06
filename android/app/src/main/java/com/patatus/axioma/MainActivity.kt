package com.patatus.axioma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.features.auth.di.AuthModule
import com.patatus.axioma.features.auth.presentation.screens.LoginScreen
import com.patatus.axioma.features.auth.presentation.viewmodels.LoginViewModel

import com.patatus.axioma.features.auth.presentation.screens.RegisterScreen
import com.patatus.axioma.features.auth.presentation.viewmodels.RegisterViewModel

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.patatus.axioma.ui.theme.AppTheme

import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = AppContainer(applicationContext)

        setContent {
            AppTheme {
                val authModule = AuthModule(appContainer)

                var isLoginScreen by remember { mutableStateOf(false) }

                if (isLoginScreen) {
                    val viewModel: LoginViewModel = viewModel(
                        factory = authModule.provideLoginViewModelFactory()
                    )

                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateHome = {
                            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_LONG).show()
                        },
                    )

                } else {

                    val viewModel: RegisterViewModel = viewModel(
                        factory = authModule.provideRegisterViewModelFactory()
                    )
                    RegisterScreen(
                        viewModel = viewModel,
                        onRegisterSuccess = {
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            isLoginScreen = true
                        },
                        onNavigateToLogin = {
                            isLoginScreen = true
                        }
                    )
                }
            }
        }
    }
}