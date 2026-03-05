package com.patatus.axioma.core.di

import com.patatus.axioma.BuildConfig
import com.patatus.axioma.core.config.AppConfig
import com.patatus.axioma.core.network.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
            val token = TokenManager.getToken()
            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }
    }
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(AppConfig.network.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(AppConfig.network.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.network.writeTimeoutSeconds, TimeUnit.SECONDS)
            .build()
    }
    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_API)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}