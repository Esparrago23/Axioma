package com.patatus.axioma.features.reports.domain.repositories
import com.patatus.axioma.features.reports.domain.entities.Report
interface ReportsRepository {
    suspend fun createReport(
        title: String,
        desc: String,
        lat: Double,
        long: Double,
        category: String
    ): Result<Report>
}