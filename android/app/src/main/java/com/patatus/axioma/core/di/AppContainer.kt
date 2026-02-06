package com.patatus.axioma.core.di

import android.content.Context
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.core.network.AuthApiService
import com.patatus.axioma.core.network.ReportsApiService
import com.patatus.axioma.features.auth.data.repositories.AuthRepositoryImpl
import com.patatus.axioma.features.reports.data.repositories.ReportsRepositoryImpl
import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import com.patatus.axioma.core.network.TokenManager
class AppContainer(context: Context) {
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        val token = TokenManager.getToken()
        if (token != null) {
            request.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(request.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    //auth
    private val authretrofit = createRetrofit(BuildConfig.BASE_URL_API)
    private val authApiService: AuthApiService by lazy {
        authretrofit.create(AuthApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }
    //reports
    private val reportsRetrofit = createRetrofit(BuildConfig.BASE_URL_API)

    val reportsApiService: ReportsApiService by lazy {
        reportsRetrofit.create(ReportsApiService::class.java)
    }

    val reportsRepository: ReportsRepository by lazy {
        ReportsRepositoryImpl(reportsApiService)
    }
}