package com.patatus.axioma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.core.navigation.AppNavigation
import com.patatus.axioma.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = AppContainer(applicationContext)

        setContent {
            AppTheme {
                AppNavigation.Graph(appContainer = appContainer)
            }
        }
    }
}