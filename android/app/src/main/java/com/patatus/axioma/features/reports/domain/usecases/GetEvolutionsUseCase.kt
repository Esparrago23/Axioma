package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.ReportEvolution
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class GetEvolutionsUseCase @Inject constructor(private val repo: ReportsRepository) {
    suspend operator fun invoke(reportId: Int): Result<List<ReportEvolution>> = repo.getEvolutions(reportId)
}
