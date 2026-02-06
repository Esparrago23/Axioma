package com.patatus.axioma.features.reports.data.repositories

import com.patatus.axioma.core.network.ReportsApiService
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
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
}