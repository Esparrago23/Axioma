package com.patatus.axioma.features.reports.domain.entities

sealed interface ReportRealtimeEvent {
    data class NewReport(val report: Report) : ReportRealtimeEvent

    data class VoteUpdate(
        val reportId: Int,
        val credibilityScore: Int,
        val status: String
    ) : ReportRealtimeEvent
}