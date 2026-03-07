package com.patatus.axioma.features.auth.domain.usecases

import com.patatus.axioma.features.auth.domain.entities.User
import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class QuickLoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> = repository.quickLoginWithStoredSession()
}
