package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class GetReportsMapUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(lat: Double, long: Double, radiusKm: Int = 15, category: String? = null): Result<List<Report>> =
        repo.getReportsMap(lat, long, radiusKm, category)
}