package com.patatus.axioma.features.reports.domain.usecases
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository

class UpdateReportUseCase(private val repo: ReportsRepository) {
    suspend operator fun invoke(id: Int, title: String?, desc: String?): Result<Report> {
        return repo.updateReport(id, title, desc)
    }
}