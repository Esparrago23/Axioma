package com.patatus.axioma.features.reports.domain.repositories

import androidx.paging.PagingData
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.Report
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
    suspend fun getReportsMap(lat: Double, long: Double): Result<List<Report>>
    suspend fun getReportDetail(id: Int): Result<Report>

    suspend fun updateReport(id: Int, title: String?, desc: String?): Result<Report>

    suspend fun deleteReport(id: Int): Result<Boolean>

    suspend fun voteReport(id: Int, isUpvote: Boolean): Result<VoteResponse>
}