package com.patatus.axioma.features.users.domain.usecases

import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UsersRepository
) {
    suspend operator fun invoke(): Result<User> = repository.getMyProfile()
}
