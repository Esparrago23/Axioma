package com.patatus.axioma.core.di

import com.patatus.axioma.BuildConfig
import com.patatus.axioma.core.config.AppConfig
import com.patatus.axioma.core.network.SecureSessionStore
import com.patatus.axioma.core.network.TokenManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        secureSessionStore: SecureSessionStore
    ): Interceptor {
        val refreshLock = Any()

        val refreshClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Interceptor { chain ->
            val originalRequest = chain.request()
            val currentToken = TokenManager.getToken()

            val requestWithAuth = if (currentToken != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            } else {
                originalRequest
            }

            val response = chain.proceed(requestWithAuth)

            // No reintentar: no es 401, es un endpoint de auth, o ya se reintentó
            if (response.code != 401 ||
                originalRequest.url.encodedPath.contains("auth/") ||
                originalRequest.header("X-Token-Retry") != null
            ) {
                return@Interceptor response
            }

            val newToken = synchronized(refreshLock) {
                val tokenAfterLock = TokenManager.getToken()
                // Otro hilo ya refrescó el token mientras esperábamos el lock
                if (tokenAfterLock != null && tokenAfterLock != currentToken) {
                    return@synchronized tokenAfterLock
                }

                val refreshToken = secureSessionStore.getRefreshToken()
                    ?: run {
                        TokenManager.clearToken()
                        return@synchronized null
                    }

                try {
                    val bodyJson = org.json.JSONObject()
                        .put("refresh_token", refreshToken)
                        .put("device_name", "android")
                        .toString()

                    val refreshRequest = Request.Builder()
                        .url("${BuildConfig.BASE_URL_API}auth/refresh")
                        .post(bodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()

                    val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                    if (refreshResponse.isSuccessful) {
                        val json = org.json.JSONObject(refreshResponse.body?.string() ?: "")
                        val newAccessToken = json.getString("access_token")
                        val newRefreshToken = json.getString("refresh_token")
                        TokenManager.saveToken(newAccessToken)
                        secureSessionStore.saveRefreshToken(newRefreshToken)
                        newAccessToken
                    } else {
                        refreshResponse.close()
                        TokenManager.clearToken()
                        secureSessionStore.clearRefreshToken()
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (newToken == null) return@Interceptor response

            response.close()

            val retryRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .header("X-Token-Retry", "true")
                .build()

            chain.proceed(retryRequest)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val uploadTimeoutInterceptor = Interceptor { chain ->
            if (chain.request().body is MultipartBody) {
                chain
                    .withWriteTimeout(120, TimeUnit.SECONDS)
                    .withReadTimeout(120, TimeUnit.SECONDS)
                    .proceed(chain.request())
            } else {
                chain.proceed(chain.request())
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(uploadTimeoutInterceptor)
            .connectTimeout(AppConfig.network.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(AppConfig.network.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.network.writeTimeoutSeconds, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @WebSocketOkHttp
    fun provideWebSocketOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(AppConfig.network.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.network.writeTimeoutSeconds, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_API)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
