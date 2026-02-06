package com.patatus.axioma.features.auth.data.datasources.remote.mapper

import com.patatus.axioma.features.auth.data.datasources.remote.models.TokenResponse
import com.patatus.axioma.features.auth.domain.entities.User

fun TokenResponse.toDomain(): User {
    return User(
        userId = this.userId,
        username = this.username,
        accessToken = this.accessToken,
        reputation = this.reputation
    )
}