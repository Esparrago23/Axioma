package com.patatus.axioma.features.auth.data.datasources.remote.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val reputation: Int
)