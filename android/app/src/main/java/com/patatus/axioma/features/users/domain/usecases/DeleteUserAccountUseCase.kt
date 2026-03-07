package com.patatus.axioma.features.users.domain.usecases

import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import javax.inject.Inject

class DeleteUserAccountUseCase @Inject constructor(
    private val repository: UsersRepository
) {
    suspend operator fun invoke(): Result<Boolean> = repository.deleteMyAccount()
}
