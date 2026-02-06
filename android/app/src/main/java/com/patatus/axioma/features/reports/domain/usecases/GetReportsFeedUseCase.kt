package com.patatus.axioma.features.reports.domain.usecases
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository

class GetReportsFeedUseCase(private val repo: ReportsRepository) {
    suspend operator fun invoke(offset: Int = 0) = repo.getReportsFeed(offset)
}