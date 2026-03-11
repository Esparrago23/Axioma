package com.patatus.axioma.core.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SecureSessionStore"
        private const val PREFS_NAME = "axioma_secure_session"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEYSET_PREFS_KEY = "__androidx_security_crypto_encrypted_prefs_key_keyset__"
        private const val KEYSET_PREFS_VALUE = "__androidx_security_crypto_encrypted_prefs_value_keyset__"
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun recoverPrefsAfterCryptoFailure(error: Throwable): SharedPreferences {
        Log.w(TAG, "Encrypted preferences corrupted. Resetting secure session store.", error)

        // Common recovery when Android Keystore/keyset gets out-of-sync after restore/update.
        context.deleteSharedPreferences(PREFS_NAME)
        context.deleteSharedPreferences(KEYSET_PREFS_KEY)
        context.deleteSharedPreferences(KEYSET_PREFS_VALUE)

        return runCatching {
            createEncryptedPrefs()
        }.getOrElse { fallbackError ->
            Log.e(
                TAG,
                "Encrypted preferences could not be recreated. Falling back to plain preferences.",
                fallbackError
            )
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private val prefs: SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            recoverPrefsAfterCryptoFailure(e)
        }
    }

    fun saveRefreshToken(refreshToken: String) {
        runCatching {
            prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
        }.onFailure {
            Log.w(TAG, "saveRefreshToken failed; secure session was reset", it)
            val recoveredPrefs = recoverPrefsAfterCryptoFailure(it)
            recoveredPrefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
        }
    }

    fun getRefreshToken(): String? {
        return runCatching {
            prefs.getString(KEY_REFRESH_TOKEN, null)
        }.getOrElse {
            Log.w(TAG, "getRefreshToken failed; secure session was reset", it)
            recoverPrefsAfterCryptoFailure(it)
            null
        }
    }

    fun clearRefreshToken() {
        runCatching {
            prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
        }.onFailure {
            Log.w(TAG, "clearRefreshToken failed; secure session was reset", it)
            recoverPrefsAfterCryptoFailure(it)
        }
    }
}