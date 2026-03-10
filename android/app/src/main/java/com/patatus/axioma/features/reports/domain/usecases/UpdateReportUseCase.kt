package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class UpdateReportUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(id: Int, title: String, desc: String, photoUrl: String?): Result<Report> {
        return repository.updateReport(id, title, desc, photoUrl)
    }
}