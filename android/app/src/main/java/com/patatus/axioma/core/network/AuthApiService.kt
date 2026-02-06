package com.patatus.axioma.core.network

import com.patatus.axioma.features.auth.data.datasources.remote.models.LoginRequest
import com.patatus.axioma.features.auth.data.datasources.remote.models.TokenResponse
import com.patatus.axioma.features.auth.data.datasources.remote.models.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("auth/register")
    suspend fun register(@Body request: LoginRequest): UserResponse

}