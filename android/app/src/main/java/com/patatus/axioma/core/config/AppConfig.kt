package com.patatus.axioma.core.config

import com.patatus.axioma.BuildConfig

data class NetworkConfig(
    val apiBaseUrl: String,
    val connectTimeoutSeconds: Long,
    val readTimeoutSeconds: Long,
    val writeTimeoutSeconds: Long
)

object AppConfig {
    val network: NetworkConfig by lazy {
        NetworkConfig(
            apiBaseUrl = BuildConfig.BASE_URL_API,
            connectTimeoutSeconds = 30L,
            readTimeoutSeconds = 30L,
            writeTimeoutSeconds = 30L
        )
    }
}