package com.patatus.axioma

import android.app.Application
import com.patatus.axioma.core.network.SecureSessionStore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AxiomaApp : Application() {
	override fun onCreate() {
		super.onCreate()
		SecureSessionStore.initialize(this)
	}
}