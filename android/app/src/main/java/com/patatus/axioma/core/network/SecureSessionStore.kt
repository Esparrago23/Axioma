package com.patatus.axioma.core.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureSessionStore {
    private const val PREFS_NAME = "axioma_secure_session"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    @Volatile
    private var initialized = false
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            appContext = context.applicationContext
            initialized = true
        }
    }

    private fun prefs() = EncryptedSharedPreferences.create(
        appContext,
        PREFS_NAME,
        MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveRefreshToken(refreshToken: String) {
        if (!initialized) return
        prefs().edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    fun getRefreshToken(): String? {
        if (!initialized) return null
        return prefs().getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearRefreshToken() {
        if (!initialized) return
        prefs().edit().remove(KEY_REFRESH_TOKEN).apply()
    }
}
