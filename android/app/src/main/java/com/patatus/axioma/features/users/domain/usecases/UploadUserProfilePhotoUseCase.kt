package com.patatus.axioma.features.users.domain.usecases

import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import javax.inject.Inject

class UploadUserProfilePhotoUseCase @Inject constructor(
    private val repository: UsersRepository
) {
    suspend operator fun invoke(localUri: String): Result<User> {
        return repository.uploadMyProfilePhoto(localUri)
    }
}
