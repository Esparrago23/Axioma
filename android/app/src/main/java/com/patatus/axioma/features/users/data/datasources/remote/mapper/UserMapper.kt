package com.patatus.axioma.features.users.data.datasources.remote.mapper

import com.patatus.axioma.features.users.data.datasources.remote.models.UserResponse
import com.patatus.axioma.features.users.domain.entities.User

fun UserResponse.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        reputation = reputationScore,
        profilePicture = profilePictureUrl,
        fullName = fullName,
        createdAt = createdAt
    )
}
