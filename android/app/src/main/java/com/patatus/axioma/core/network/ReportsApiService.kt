package com.patatus.axioma.core.network

import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportsApiService {
    @POST("reports/")
    suspend fun createReport(@Body report: ReportCreateRequest): ReportResponse
}