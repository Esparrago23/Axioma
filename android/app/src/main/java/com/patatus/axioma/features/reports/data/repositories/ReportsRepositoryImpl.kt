package com.patatus.axioma.features.reports.data.repositories

import android.content.Context
import androidx.core.net.toUri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.reports.data.datasources.local.db.daos.ReportDao
import com.patatus.axioma.features.reports.data.datasources.remote.api.ReportsApiService
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toEntity
import com.patatus.axioma.features.reports.data.datasources.remote.mediator.ReportRemoteMediator
import com.patatus.axioma.features.reports.data.datasources.remote.models.CreateEvolutionRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.EvolutionVoteRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportCreateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportUpdateRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteRequest
import com.patatus.axioma.features.reports.data.datasources.remote.models.VoteResponse
import com.patatus.axioma.features.reports.data.realtime.ReportsRealtimeWebSocketDataSource
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.FeedSort
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.entities.ReportEvolution
import com.patatus.axioma.features.reports.domain.entities.ReportRealtimeEvent
import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val reportDao: ReportDao,
    private val database: AxiomaDatabase,
    private val realtimeDataSource: ReportsRealtimeWebSocketDataSource,
    @ApplicationContext private val context: Context
) : ReportsRepository {


    override suspend fun createReport(
        title: String,
        desc: String,
        lat: Double,
        long: Double,
        category: String,
        photoUrl: String?
    ): Result<Report> {
        val request = ReportCreateRequest(
            title = title,
            description = desc,
            category = category,
            latitude = lat,
            longitude = long,
            photoUrl = photoUrl
        )
        return safeApiCall { api.createReport(request).toDomain() }
    }

    override suspend fun uploadReportPhoto(localUri: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = localUri.toUri()
                val tempFile = File.createTempFile("report_upload_", ".jpg", context.cacheDir)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val originalBitmap = android.graphics.BitmapFactory.decodeStream(input)
                        ?: return@withContext Result.failure(Exception("No se pudo leer la imagen"))

                    val maxSize = 1080
                    val ratio = kotlin.math.min(
                        maxSize.toFloat() / originalBitmap.width,
                        maxSize.toFloat() / originalBitmap.height
                    )

                    val scaledBitmap = if (ratio < 1f) {
                        android.graphics.Bitmap.createScaledBitmap(
                            originalBitmap,
                            (originalBitmap.width * ratio).toInt(),
                            (originalBitmap.height * ratio).toInt(),
                            true
                        )
                    } else {
                        originalBitmap
                    }

                    tempFile.outputStream().use { output ->
                        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output)
                    }

                    if (scaledBitmap != originalBitmap) {
                        scaledBitmap.recycle()
                    }
                    originalBitmap.recycle()
                }

                val body = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
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

    override suspend fun getReportsMap(lat: Double, long: Double, radiusKm: Int, category: String?): Result<List<Report>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getReportsNearby(
                    latitude = lat,
                    longitude = long,
                    radiusKm = radiusKm,
                    sort = "recent",
                    limit = 100,
                    offset = 0,
                    category = category
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
        return safeApiCall {
            val request = ReportUpdateRequest(
                title = title,
                description = desc,
                photoUrl = photoUrl
            )
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

    override suspend fun getMyReports(search: String?): Result<List<Report>> {
        return safeApiCall {
            api.getMyReports(search).map { it.toDomain() }
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

    override suspend fun getEvolutions(reportId: Int): Result<List<ReportEvolution>> = safeApiCall {
        api.getEvolutions(reportId).map { it.toDomain() }
    }

    override suspend fun createEvolution(
        reportId: Int,
        type: String,
        description: String,
        photoUrl: String?,
        userLat: Double,
        userLon: Double,
    ): Result<ReportEvolution> = safeApiCall {
        api.createEvolution(
            reportId,
            CreateEvolutionRequest(
                type = type,
                description = description,
                photoUrl = photoUrl,
                userLatitude = userLat,
                userLongitude = userLon,
            )
        ).toDomain()
    }

    override suspend fun voteEvolution(evolutionId: Int, isUpvote: Boolean): Result<ReportEvolution> = safeApiCall {
        api.voteEvolution(evolutionId, EvolutionVoteRequest(if (isUpvote) 1 else -1)).toDomain()
    }

    override suspend fun deleteEvolution(evolutionId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteEvolution(evolutionId)
                if (response.isSuccessful) Result.success(true)
                else {
                    val msg = try { JSONObject(response.errorBody()?.string()).getString("detail") } catch (e: Exception) { "Error al eliminar" }
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Result.failure(e)
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