package com.patatus.axioma.core.di

import android.content.Context
import com.patatus.axioma.core.network.AuthApiService
import com.patatus.axioma.features.auth.data.repositories.AuthRepositoryImpl
import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://esparrago.engineer")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }
}