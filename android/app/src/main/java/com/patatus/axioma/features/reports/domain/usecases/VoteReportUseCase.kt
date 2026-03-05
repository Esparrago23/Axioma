package com.patatus.axioma.features.reports.domain.usecases
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class VoteReportUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(id: Int, isUpvote: Boolean) = repo.voteReport(id, isUpvote)
}