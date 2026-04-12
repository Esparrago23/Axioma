package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class DeleteEvolutionUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(evolutionId: Int): Result<Boolean> =
        repository.deleteEvolution(evolutionId)
}
