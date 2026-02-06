package com.patatus.axioma.core.network

import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportsApiService {
    @POST("reports/")
    suspend fun createReport(@Body report: ReportCreateRequest): ReportResponse

    @GET("reports/all")
    suspend fun getAllReports(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<ReportResponse>

    @GET("reports/")
    suspend fun getReportsByLocation(
        @Query("lat") lat: Double,
        @Query("long") long: Double,
        @Query("radius_meters") radius: Double = 2000.0
    ): List<ReportResponse>

    @GET("reports/{id}")
    suspend fun getReportDetail(@Path("id") id: Int): ReportResponse

    @PATCH("reports/{id}")
    suspend fun updateReport(
        @Path("id") id: Int,
        @Body update: ReportUpdateRequest
    ): ReportResponse

    @DELETE("reports/{id}")
    suspend fun deleteReport(@Path("id") id: Int): Void?

    @POST("reports/{id}/vote")
    suspend fun voteReport(
        @Path("id") id: Int,
        @Body vote: VoteRequest
    ): VoteResponse
}