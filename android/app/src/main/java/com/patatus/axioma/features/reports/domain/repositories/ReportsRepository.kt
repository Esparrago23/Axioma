package com.patatus.axioma.features.reports.domain.repositories

import androidx.paging.PagingData
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.entities.ReportEvolution
import com.patatus.axioma.features.reports.domain.entities.ReportRealtimeEvent
import kotlinx.coroutines.flow.Flow

interface ReportsRepository {
    suspend fun createReport(
        title: String,
        desc: String,
        lat: Double,
        long: Double,
        category: String,
        photoUrl: String? = null
    ): Result<Report>

    suspend fun uploadReportPhoto(localUri: String): Result<String>
    fun getReportsFeed(query: FeedQuery): Flow<PagingData<Report>>
    suspend fun getReportsMap(lat: Double, long: Double, radiusKm: Int = 15, category: String? = null): Result<List<Report>>
    suspend fun getReportDetail(id: Int): Result<Report>
    suspend fun updateReport(id: Int, title: String, desc: String, photoUrl: String?): Result<Report>
    suspend fun deleteReport(id: Int): Result<Boolean>
    suspend fun voteReport(id: Int, isUpvote: Boolean): Result<VoteResponse>
    suspend fun getMyReports(search: String? = null): Result<List<Report>>
    fun observeRealtimeEvents(): Flow<ReportRealtimeEvent>
    suspend fun applyRealtimeEvent(event: ReportRealtimeEvent)

    suspend fun getEvolutions(reportId: Int): Result<List<ReportEvolution>>
    suspend fun createEvolution(
        reportId: Int,
        type: String,
        description: String,
        photoUrl: String?,
        userLat: Double,
        userLon: Double,
    ): Result<ReportEvolution>
    suspend fun voteEvolution(evolutionId: Int, isUpvote: Boolean): Result<ReportEvolution>
    suspend fun deleteEvolution(evolutionId: Int): Result<Boolean>
}