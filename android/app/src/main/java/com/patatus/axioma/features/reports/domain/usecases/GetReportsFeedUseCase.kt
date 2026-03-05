package com.patatus.axioma.features.reports.domain.usecases
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class GetReportsFeedUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(offset: Int = 0) = repo.getReportsFeed(offset)
}