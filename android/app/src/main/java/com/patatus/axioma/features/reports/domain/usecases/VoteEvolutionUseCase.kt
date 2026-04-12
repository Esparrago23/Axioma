package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.ReportEvolution
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class VoteEvolutionUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(evolutionId: Int, isUpvote: Boolean): Result<ReportEvolution> =
        repo.voteEvolution(evolutionId, isUpvote)
}
