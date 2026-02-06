package com.patatus.axioma.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.patatus.axioma.core.di.AppContainer

object AppNavigation {

    object Routes {
        const val LOGIN = "login"
        const val REGISTER = "register"
        const val FEED = "feed"
        const val CREATE_REPORT = "create_report"
        const val REPORT_DETAIL = "report_detail/{reportId}"

        fun reportDetail(id: Int) = "report_detail/$id"
    }

    @Composable
    fun Graph(appContainer: AppContainer) {
        val navController = rememberNavController()

        val screens = remember { AppScreens(appContainer) }

        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN
        ) {

            composable(Routes.LOGIN) {
                screens.Login(navController)
            }

            composable(Routes.REGISTER) {
                screens.Register(navController)
            }

            composable(Routes.FEED) {
                screens.Feed(navController)
            }

            composable(Routes.CREATE_REPORT) {
                screens.CreateReport(navController)
            }

            composable(
                route = Routes.REPORT_DETAIL,
                arguments = listOf(navArgument("reportId") { type = NavType.IntType })
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getInt("reportId") ?: 0
                screens.ReportDetail(navController, reportId)
            }
        }
    }
}