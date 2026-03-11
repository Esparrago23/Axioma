package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class ObserveReportRealtimeEventsUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    operator fun invoke() = repository.observeRealtimeEvents()
}