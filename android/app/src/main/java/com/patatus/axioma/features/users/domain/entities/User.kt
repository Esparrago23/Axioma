package com.patatus.axioma.features.users.domain.entities

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val reputation: Int,
    val profilePicture: String?,
    val fullName: String?,
    val createdAt: String
)


