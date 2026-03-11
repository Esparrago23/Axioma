package com.patatus.axioma.features.reports.data.repositories

import android.content.Context
import androidx.core.net.toUri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.qualifiers.ApplicationContext
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.reports.data.datasources.remote.api.ReportsApiService
import com.patatus.axioma.features.reports.data.datasources.remote.mediator.ReportRemoteMediator
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toEntity
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportUpdateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse
import com.patatus.axioma.features.reports.data.realtime.ReportsRealtimeWebSocketDataSource
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.FeedSort
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.entities.ReportRealtimeEvent
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val api: ReportsApiService,
    private val database: AxiomaDatabase,
    private val realtimeDataSource: ReportsRealtimeWebSocketDataSource,
    @ApplicationContext private val context: Context
) : ReportsRepository {

    private val reportDao = database.reportDao()

    override suspend fun createReport(
        title: String,
        desc: String,
        lat: Double,
        long: Double,
        category: String,
        photoUrl: String?
    ): Result<Report> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ReportCreateRequest(
                    title = title,
                    description = desc,
                    category = category,
                    latitude = lat,
                    longitude = long,
                    photoUrl = photoUrl
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

    override suspend fun uploadReportPhoto(localUri: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = localUri.toUri()
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = mimeType.substringAfter('/', "jpg")
                val tempFile = File.createTempFile("report_upload_", ".${extension}", context.cacheDir)

                context.contentResolver.openInputStream(uri).use { input ->
                    if (input == null) {
                        return@withContext Result.failure(Exception("No se pudo leer la imagen seleccionada"))
                    }
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }

                val body = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("photo", tempFile.name, body)
                val response = api.uploadReportPhoto(part)
                tempFile.delete()

                Result.success(response.photoUrl)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val msg = try { JSONObject(errorBody).getString("detail") } catch (ex: Exception) { "Error del servidor" }
                Result.failure(Exception(msg))
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "No se pudo subir la evidencia"))
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

    override suspend fun updateReport(id: Int, title: String, desc: String, photoUrl: String?): Result<Report> {
        return try {
            val request = ReportUpdateRequest(
                title = title,
                description = desc,
                photoUrl = photoUrl
            )
            val response = api.updateReport(id, request)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
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

    override fun observeRealtimeEvents(): Flow<ReportRealtimeEvent> {
        return realtimeDataSource.observeEvents()
    }

    override suspend fun applyRealtimeEvent(event: ReportRealtimeEvent) {
        withContext(Dispatchers.IO) {
            when (event) {
                is ReportRealtimeEvent.NewReport -> {
                    val existingReport = reportDao.getById(event.report.id)
                    reportDao.insert(
                        event.report.toEntity(userVote = existingReport?.userVote ?: 0)
                    )
                }

                is ReportRealtimeEvent.VoteUpdate -> {
                    reportDao.updateRealtimeVote(
                        reportId = event.reportId,
                        credibilityScore = event.credibilityScore,
                        status = event.status
                    )
                }
            }
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