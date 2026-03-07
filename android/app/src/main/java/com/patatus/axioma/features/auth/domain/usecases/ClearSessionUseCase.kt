package com.patatus.axioma.features.auth.domain.usecases

import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class ClearSessionUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.clearStoredSession()
}
