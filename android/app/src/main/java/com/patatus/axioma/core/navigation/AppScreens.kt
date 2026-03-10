package com.patatus.axioma.core.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.patatus.axioma.features.auth.presentation.screens.LoginScreen
import com.patatus.axioma.features.auth.presentation.screens.RegisterScreen
import com.patatus.axioma.features.auth.presentation.viewmodels.AuthViewModel
import com.patatus.axioma.features.reports.presentation.screens.CreateReportScreen
import com.patatus.axioma.features.reports.presentation.screens.FeedScreen
import com.patatus.axioma.features.reports.presentation.screens.ReportDetailScreen
import com.patatus.axioma.features.reports.presentation.viewmodels.CreateReportViewModel
import com.patatus.axioma.features.reports.presentation.viewmodels.FeedViewModel
import com.patatus.axioma.features.reports.presentation.viewmodels.ReportDetailViewModel
import com.patatus.axioma.features.users.presentation.screens.ProfileScreen
import com.patatus.axioma.features.users.presentation.viewmodels.ProfileViewModel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class AppScreens(

) {
    /* ---------------- LOGIN ---------------- */

    @Composable
    fun Login(navController: NavController) {

        val viewModel: AuthViewModel = hiltViewModel()

        LoginScreen(
            viewModel = viewModel,
            onNavigateHome = {
                navController.navigate(AppNavigation.Routes.FEED) {
                    popUpTo(AppNavigation.Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToRegister = {
                navController.navigate(AppNavigation.Routes.REGISTER)
            }
        )
    }

    /* ---------------- REGISTER ---------------- */

    @Composable
    fun Register(navController: NavController) {

        val viewModel: AuthViewModel = hiltViewModel()

        RegisterScreen(
            viewModel = viewModel,
            onRegisterSuccess = {
                navController.navigate(AppNavigation.Routes.LOGIN) {
                    popUpTo(AppNavigation.Routes.REGISTER) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToLogin = {
                navController.navigateUp()
            }
        )
    }

    /* ---------------- FEED ---------------- */

    @Composable
    fun Feed(navController: NavController) {

        val viewModel: FeedViewModel = hiltViewModel()

        FeedScreen(
            viewModel = viewModel,
            onNavigateToCreate = {
                navController.navigate(AppNavigation.Routes.CREATE_REPORT)
            },
            onNavigateToProfile = {
                navController.navigate(AppNavigation.Routes.PROFILE)
            },
            onNavigateToDetail = { reportId ->
                navController.navigate(
                    AppNavigation.Routes.reportDetail(reportId)
                )
            }
        )
    }

    /* ---------------- CREATE REPORT ---------------- */

    @Composable
    fun CreateReport(navController: NavController) {

        val viewModel: CreateReportViewModel = hiltViewModel()
        CreateReportScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }

    /* ---------------- REPORT DETAIL ---------------- */

    @Composable
    fun ReportDetail(
        navController: NavController,
        reportId: Int
    ) {
        val viewModel: ReportDetailViewModel = hiltViewModel()
        ReportDetailScreen(
            reportId = reportId,
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }

    /* ---------------- MAPA (Placeholder) ---------------- */
    @Composable
    fun Mapa(navController: NavController) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pantalla de Mapa en construcción")
        }
    }

    /* ---------------- MIS REPORTES (Placeholder) ---------------- */
    @Composable
    fun MisReportes(navController: NavController) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pantalla de Mis Reportes en construcción")
        }
    }

    @Composable
    fun Profile(navController: NavController) {
        val viewModel: ProfileViewModel = hiltViewModel()
        ProfileScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onAccountDeleted = {
                navController.navigate(AppNavigation.Routes.LOGIN) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
        )
    }
}
