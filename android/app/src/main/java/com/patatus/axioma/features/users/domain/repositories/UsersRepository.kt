package com.patatus.axioma.features.users.domain.repositories

import com.patatus.axioma.features.users.domain.entities.User

interface UsersRepository {
    suspend fun getMyProfile(): Result<User>

    suspend fun updatePushRegistration(
        fcmToken: String? = null,
        lastLatitude: Double? = null,
        lastLongitude: Double? = null
    ): Result<Unit>

    suspend fun updateMyProfile(
        username: String?,
        fullName: String?,
        profilePictureUrl: String?
    ): Result<User>

    suspend fun uploadMyProfilePhoto(localUri: String): Result<User>

    suspend fun deleteMyAccount(): Result<Boolean>
}
