package com.patatus.axioma.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.features.auth.di.AuthModule
import com.patatus.axioma.features.auth.presentation.screens.LoginScreen
import com.patatus.axioma.features.auth.presentation.screens.RegisterScreen
import com.patatus.axioma.features.auth.presentation.viewmodels.LoginViewModel
import com.patatus.axioma.features.auth.presentation.viewmodels.RegisterViewModel
import com.patatus.axioma.features.reports.di.ReportsModule
import com.patatus.axioma.features.reports.presentation.screens.CreateReportScreen
import com.patatus.axioma.features.reports.presentation.screens.FeedScreen
import com.patatus.axioma.features.reports.presentation.screens.ReportDetailScreen
import com.patatus.axioma.features.reports.presentation.viewmodels.CreateReportViewModel
import com.patatus.axioma.features.reports.presentation.viewmodels.FeedViewModel
import com.patatus.axioma.features.reports.presentation.viewmodels.ReportDetailViewModel

class AppScreens(
    private val appContainer: AppContainer
) {

    @Composable
    private fun rememberAuthModule() =
        remember(appContainer) { AuthModule(appContainer) }

    @Composable
    private fun rememberReportsModule() =
        remember(appContainer) { ReportsModule(appContainer) }

    /* ---------------- LOGIN ---------------- */

    @Composable
    fun Login(navController: NavController) {

        val authModule = rememberAuthModule()

        val viewModel: LoginViewModel = viewModel(
            factory = remember(authModule) {
                authModule.provideLoginViewModelFactory()
            }
        )

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

        val authModule = rememberAuthModule()

        val viewModel: RegisterViewModel = viewModel(
            factory = remember(authModule) {
                authModule.provideRegisterViewModelFactory()
            }
        )

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

        val reportsModule = rememberReportsModule()

        val viewModel: FeedViewModel = viewModel(
            factory = remember(reportsModule) {
                reportsModule.provideFeedViewModelFactory()
            }
        )

        FeedScreen(
            viewModel = viewModel,
            onNavigateToCreate = {
                navController.navigate(AppNavigation.Routes.CREATE_REPORT)
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

        val reportsModule = rememberReportsModule()

        val viewModel: CreateReportViewModel = viewModel(
            factory = remember(reportsModule) {
                reportsModule.provideCreateReportViewModelFactory()
            }
        )

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

        val reportsModule = rememberReportsModule()

        val viewModel: ReportDetailViewModel = viewModel(
            factory = remember(reportsModule) {
                reportsModule.provideReportDetailViewModelFactory()
            }
        )

        ReportDetailScreen(
            reportId = reportId,
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }
}
