package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class CreateReportUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(title: String, desc: String, lat: Double, long: Double, cat: String): Result<Report> {
        return repo.createReport(title, desc, lat, long, cat)
    }
}