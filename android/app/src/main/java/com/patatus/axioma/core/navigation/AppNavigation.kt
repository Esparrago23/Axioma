package com.patatus.axioma.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Clase auxiliar para definir los items de la barra inferior
data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

object AppNavigation {

    object Routes {
        const val LOGIN = "login"
        const val REGISTER = "register"
        const val FEED = "feed"
        const val MAPA = "mapa" // NUEVA
        const val MIS_REPORTES = "mis_reportes" // NUEVA
        const val PROFILE = "profile"
        const val CREATE_REPORT = "create_report"
        const val REPORT_DETAIL = "report_detail/{reportId}"

        fun reportDetail(id: Int) = "report_detail/$id"
    }

    @Composable
    fun Graph() {
        val navController = rememberNavController()
        val screens = remember { AppScreens() }

        // Observar la ruta actual de navegación
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Solo mostramos la barra inferior en estas rutas específicas
        val bottomBarRoutes = listOf(Routes.FEED, Routes.MAPA, Routes.MIS_REPORTES)
        val showBottomBar = currentRoute in bottomBarRoutes

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        val bottomNavItems = listOf(
                            BottomNavItem(Routes.FEED, "Inicio", Icons.Default.Home),
                            BottomNavItem(Routes.MAPA, "Mapa", Icons.Default.Map),
                            BottomNavItem(Routes.MIS_REPORTES, "Mis Reportes", Icons.Default.List),
                            BottomNavItem(Routes.CREATE_REPORT, "Reportar", Icons.Default.AddCircle)
                        )

                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        navController.navigate(item.route) {
                                            // PopUp hasta el inicio para no acumular vistas infinitas en el stack
                                            popUpTo(Routes.FEED) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.LOGIN,
                modifier = Modifier.padding(innerPadding) // <-- MUY IMPORTANTE PARA QUE LA BARRA NO TAPE EL CONTENIDO
            ) {

                composable(Routes.LOGIN) { screens.Login(navController) }
                composable(Routes.REGISTER) { screens.Register(navController) }
                composable(Routes.FEED) { screens.Feed(navController) }

                // Nuevas rutas agregadas al NavHost
                composable(Routes.MAPA) { screens.Mapa(navController) }
                composable(Routes.MIS_REPORTES) { screens.MisReportes(navController) }

                composable(Routes.PROFILE) { screens.Profile(navController) }
                composable(Routes.CREATE_REPORT) { screens.CreateReport(navController) }

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
}