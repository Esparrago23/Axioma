package com.patatus.axioma.features.reports.data.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.reports.data.datasources.remote.api.ReportsApiService
import com.patatus.axioma.features.reports.data.datasources.remote.mediator.ReportRemoteMediator
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportUpdateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.FeedSort
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val api: ReportsApiService,
    private val database: AxiomaDatabase
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

    @OptIn(ExperimentalPagingApi::class)
    override fun getReportsFeed(query: FeedQuery): Flow<PagingData<Report>> {
        val pagingSourceFactory = {
            when (query.sort) {
                FeedSort.RELEVANT -> database.reportDao().pagingSourceRelevant(
                    sinceIso = Instant.now().minus(48, ChronoUnit.HOURS).toString()
                )
                FeedSort.RECENT -> database.reportDao().pagingSourceRecent()
            }
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = ReportRemoteMediator(
                apiService = api,
                database = database,
                query = query
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getReportsMap(lat: Double, long: Double): Result<List<Report>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getReportsNearby(
                    latitude = lat,
                    longitude = long,
                    radiusKm = 15,
                    sort = "recent",
                    limit = 100,
                    offset = 0
                )

                if (!response.isSuccessful) {
                    throw HttpException(response)
                }

                Result.success(response.body().orEmpty().map { it.toDomain() })
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val msg = try { JSONObject(errorBody).getString("detail") } catch (ex: Exception) { "Error del servidor" }
                Result.failure(Exception(msg))
            } catch (e: Exception) {
                Result.failure(e)
            }
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