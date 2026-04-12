package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.ReportEvolution
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class CreateEvolutionUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(
        reportId: Int,
        type: String,
        description: String,
        photoUrl: String?,
        userLat: Double,
        userLon: Double,
    ): Result<ReportEvolution> = repo.createEvolution(reportId, type, description, photoUrl, userLat, userLon)
}
