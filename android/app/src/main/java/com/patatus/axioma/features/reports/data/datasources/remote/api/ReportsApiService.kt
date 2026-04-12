package com.patatus.axioma.features.reports.data.datasources.remote.api

import com.patatus.axioma.features.reports.data.datasources.remote.models.CreateEvolutionRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.EvolutionResponse
import com.patatus.axioma.features.reports.data.datasources.remote.models.EvolutionVoteRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportPhotoUploadResponse
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportResponse
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportUpdateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportsApiService {
    @POST("reports/")
    suspend fun createReport(@Body report: ReportCreateRequest): ReportResponse

    @Multipart
    @POST("reports/photo")
    suspend fun uploadReportPhoto(
        @Part photo: MultipartBody.Part
    ): ReportPhotoUploadResponse

    @GET("reports/all")
    suspend fun getAllReports(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<ReportResponse>

    @GET("reports/")
    suspend fun getReportsNearby(
        @Query("lat") latitude: Double,
        @Query("long") longitude: Double,
        @Query("radius_km") radiusKm: Int = 15,
        @Query("sort") sort: String = "recent",
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("category") category: String? = null
    ): Response<List<ReportResponse>>

    @GET("reports/{id}")
    suspend fun getReportDetail(@Path("id") id: Int): ReportResponse

    @PATCH("reports/{id}")
    suspend fun updateReport(
        @Path("id") id: Int,
        @Body update: ReportUpdateRequest
    ): ReportResponse

    @DELETE("reports/{id}")
    suspend fun deleteReport(@Path("id") id: Int): Response<Unit>

    @POST("reports/{id}/vote")
    suspend fun voteReport(
        @Path("id") id: Int,
        @Body vote: VoteRequest
    ): VoteResponse

    @GET("reports/me/created")
    suspend fun getMyReports(
        @Query("search") search: String? = null
    ): List<ReportResponse>

    @GET("reports/{id}/evolutions")
    suspend fun getEvolutions(@Path("id") reportId: Int): List<EvolutionResponse>

    @POST("reports/{id}/evolutions")
    suspend fun createEvolution(
        @Path("id") reportId: Int,
        @Body body: CreateEvolutionRequest
    ): EvolutionResponse

    @POST("reports/evolutions/{evolutionId}/vote")
    suspend fun voteEvolution(
        @Path("evolutionId") evolutionId: Int,
        @Body vote: EvolutionVoteRequest
    ): EvolutionResponse

    @DELETE("reports/evolutions/{evolutionId}")
    suspend fun deleteEvolution(@Path("evolutionId") evolutionId: Int): Response<Unit>

}