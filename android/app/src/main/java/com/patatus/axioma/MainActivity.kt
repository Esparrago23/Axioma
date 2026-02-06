package com.patatus.axioma

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.features.auth.di.AuthModule
import com.patatus.axioma.features.auth.presentation.screens.LoginScreen
import com.patatus.axioma.features.auth.presentation.screens.RegisterScreen
import com.patatus.axioma.features.reports.di.ReportsModule
import com.patatus.axioma.features.reports.presentation.screens.CreateReportScreen
import com.patatus.axioma.features.reports.presentation.screens.FeedScreen
import com.patatus.axioma.features.reports.presentation.screens.ReportDetailScreen
import com.patatus.axioma.ui.theme.AppTheme


enum class Screen {
    LOGIN,
    REGISTER,
    FEED,
    CREATE_REPORT,
    REPORT_DETAIL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = AppContainer(applicationContext)

        setContent {
            AppTheme {
                // Instanciamos los módulos una sola vez
                val authModule = remember { AuthModule(appContainer) }
                val reportsModule = remember { ReportsModule(appContainer) }

                // ESTADO DE NAVEGACIÓN
                var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
                // Para guardar el ID del reporte seleccionado
                var selectedReportId by remember { mutableStateOf<Int?>(null) }

                val context = LocalContext.current

                when (currentScreen) {
                    // --- PANTALLAS DE AUTH ---
                    Screen.LOGIN -> {
                        LoginScreen(
                            viewModel = viewModel(factory = authModule.provideLoginViewModelFactory()),
                            onNavigateHome = {
                                Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                currentScreen = Screen.FEED // <--- AQUÍ OCURRE LA MAGIA
                            }
                        )
                    }
                    Screen.REGISTER -> {
                        RegisterScreen(
                            viewModel = viewModel(factory = authModule.provideRegisterViewModelFactory()),
                            onRegisterSuccess = {
                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                currentScreen = Screen.LOGIN
                            },
                            onNavigateToLogin = {
                                currentScreen = Screen.LOGIN
                            }
                        )
                    }

                    // --- PANTALLAS DE REPORTES ---
                    Screen.FEED -> {
                        FeedScreen(
                            viewModel = viewModel(factory = reportsModule.provideFeedViewModelFactory()),
                            onNavigateToCreate = {
                                currentScreen = Screen.CREATE_REPORT
                            },
                            onNavigateToDetail = { reportId ->
                                selectedReportId = reportId
                                currentScreen = Screen.REPORT_DETAIL
                            }
                        )
                    }
                    Screen.CREATE_REPORT -> {
                        CreateReportScreen(
                            viewModel = viewModel(factory = reportsModule.provideCreateReportViewModelFactory()),
                            onBack = {
                                // Al terminar o cancelar, volvemos al Feed y refrescamos
                                currentScreen = Screen.FEED
                            }
                        )
                    }
                    Screen.REPORT_DETAIL -> {
                        if (selectedReportId != null) {
                            ReportDetailScreen(
                                reportId = selectedReportId!!,
                                viewModel = viewModel(factory = reportsModule.provideReportDetailViewModelFactory()),
                                onBack = {
                                    selectedReportId = null
                                    currentScreen = Screen.FEED
                                }
                            )
                        } else {
                            // Si por error llegamos aquí sin ID, volvemos al Feed
                            currentScreen = Screen.FEED
                        }
                    }
                }
            }
        }
    }
}