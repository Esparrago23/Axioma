package com.patatus.axioma.features.reports.domain.usecases
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository

class GetReportDetailUseCase(private val repo: ReportsRepository) {
    suspend operator fun invoke(id: Int) = repo.getReportDetail(id)
}