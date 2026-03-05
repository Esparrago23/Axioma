package com.patatus.axioma.features.auth.domain.usecases

import com.patatus.axioma.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, pass: String): Result<Boolean> {
        return repository.register(email, pass)
    }
}