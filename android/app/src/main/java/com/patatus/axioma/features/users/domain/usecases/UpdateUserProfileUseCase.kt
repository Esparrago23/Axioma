package com.patatus.axioma.features.users.domain.usecases

import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UsersRepository
) {
    suspend operator fun invoke(
        username: String?,
        fullName: String?,
        profilePictureUrl: String?
    ): Result<User> {
        return repository.updateMyProfile(
            username = username,
            fullName = fullName,
            profilePictureUrl = profilePictureUrl
        )
    }
}
