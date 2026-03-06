package com.patatus.axioma.features.reports.domain.usecases

import androidx.paging.PagingData
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReportsFeedUseCase @Inject constructor(private val repo: ReportsRepository) {
    operator fun invoke(): Flow<PagingData<Report>> = repo.getReportsFeed()
}