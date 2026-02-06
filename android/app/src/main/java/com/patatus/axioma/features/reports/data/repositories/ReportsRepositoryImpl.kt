package com.patatus.axioma.features.reports.data.repositories

import com.patatus.axioma.core.network.ReportsApiService
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportUpdateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import org.json.JSONObject

class ReportsRepositoryImpl(
    private val api: ReportsApiService
) : ReportsRepository {

    override suspend fun createReport(title: String, desc: String, lat: Double, long: Double, category: String): Result<Report> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ReportCreateRequest(
                    title = title,
                    description = desc,
                    category = category,
                    latitude = lat,
                    longitude = long
                )
                val response = api.createReport(request)
                val entity = response.toDomain()
                Result.success(entity)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody).getString("detail")
                } catch (jsonException: Exception) {
                    "Error desconocido en el servidor (${e.code()})"
                }
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getReportsFeed(offset: Int): Result<List<Report>> {
        return safeApiCall {
            api.getAllReports(offset).map { it.toDomain() }
        }
    }

    override suspend fun getReportsMap(lat: Double, long: Double): Result<List<Report>> {
        return safeApiCall {
            api.getReportsByLocation(lat, long).map { it.toDomain() }
        }
    }

    override suspend fun getReportDetail(id: Int): Result<Report> {
        return safeApiCall {
            api.getReportDetail(id).toDomain()
        }
    }

    override suspend fun updateReport(id: Int, title: String?, desc: String?): Result<Report> {
        return safeApiCall {
            val request = ReportUpdateRequest(title, desc)
            api.updateReport(id, request).toDomain()
        }
    }

    override suspend fun deleteReport(id: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteReport(id)

                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val msg = try { JSONObject(errorBody).getString("detail") } catch (e: Exception) { "Error al eliminar" }
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun voteReport(id: Int, isUpvote: Boolean): Result<VoteResponse> {
        return safeApiCall {
            val voteInt = if (isUpvote) 1 else -1

            api.voteReport(id, VoteRequest(voteInt))
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(apiCall())
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val msg = try { JSONObject(errorBody).getString("detail") } catch (ex: Exception) { "Error del servidor" }
                Result.failure(Exception(msg))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}