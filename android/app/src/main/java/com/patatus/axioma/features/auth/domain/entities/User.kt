package com.patatus.axioma.features.auth.domain.entities

data class User(
    val userId: Int,
    val username: String,
    val accessToken: String,
    val reputation: Int
)