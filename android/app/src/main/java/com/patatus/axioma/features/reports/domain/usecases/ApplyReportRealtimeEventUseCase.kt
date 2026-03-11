package com.patatus.axioma.features.reports.domain.usecases

import com.patatus.axioma.features.reports.domain.entities.ReportRealtimeEvent
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import javax.inject.Inject

class ApplyReportRealtimeEventUseCase @Inject constructor(
    private val repository: ReportsRepository
) {
    suspend operator fun invoke(event: ReportRealtimeEvent) {
        repository.applyRealtimeEvent(event)
    }
}