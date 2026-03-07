package com.patatus.axioma.features.users.domain.repositories

import com.patatus.axioma.features.users.domain.entities.User

interface UsersRepository {
    suspend fun getMyProfile(): Result<User>

    suspend fun updateMyProfile(
        username: String?,
        fullName: String?,
        profilePictureUrl: String?
    ): Result<User>

    suspend fun deleteMyAccount(): Result<Boolean>
}
