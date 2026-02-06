package com.patatus.axioma.features.reports.domain.repositories
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse

interface ReportsRepository {
    suspend fun createReport(
        title: String,
        desc: String,
        lat: Double,
        long: Double,
        category: String
    ): Result<Report>

    suspend fun getReportsFeed(offset: Int): Result<List<Report>>
    suspend fun getReportsMap(lat: Double, long: Double): Result<List<Report>>
    suspend fun getReportDetail(id: Int): Result<Report>

    suspend fun updateReport(id: Int, title: String?, desc: String?): Result<Report>

    suspend fun deleteReport(id: Int): Result<Boolean>

    suspend fun voteReport(id: Int, isUpvote: Boolean): Result<VoteResponse>
}