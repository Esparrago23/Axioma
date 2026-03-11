package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class GetMyReportsUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(search: String? = null): Result<List<Report>> {
        return repository.getMyReports(search)
    }
}