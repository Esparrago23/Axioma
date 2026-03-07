package com.patatus.axioma.features.auth.domain.usecases

import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class HasQuickSessionUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Boolean = repository.hasStoredQuickSession()
}
