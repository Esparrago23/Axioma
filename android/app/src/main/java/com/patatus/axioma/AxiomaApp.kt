package com.patatus.axioma

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AxiomaApp : Application() {
	override fun onCreate() {
		super.onCreate()
	}
}