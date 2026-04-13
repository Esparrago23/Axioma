package com.patatus.axioma.features.reports.data.datasources.remote.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportEntity
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportRemoteKeysEntity
import com.patatus.axioma.features.reports.data.datasources.remote.api.ReportsApiService
import com.patatus.axioma.features.reports.data.datasources.remote.mapper.toEntity
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.FeedSort
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ReportRemoteMediator(
	private val apiService: ReportsApiService,
	private val database: AxiomaDatabase,
	private val query: FeedQuery
) : RemoteMediator<Int, ReportEntity>() {

	private val reportDao = database.reportDao()
	private val remoteKeysDao = database.reportRemoteKeysDao()

	override suspend fun load(
		loadType: LoadType,
		state: PagingState<Int, ReportEntity>
	): MediatorResult {
		return try {
			val offset = when (loadType) {
				LoadType.REFRESH -> 0
				LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
				LoadType.APPEND -> {
					val remoteKeys = getRemoteKeyForLastItem(state)
					val nextKey = remoteKeys?.nextKey
						?: return MediatorResult.Success(endOfPaginationReached = true)
					nextKey
				}
			}

			val reportsDto = fetchReports(
				offset = offset,
				limit = state.config.pageSize
			)

			database.withTransaction {
				if (loadType == LoadType.REFRESH) {
					remoteKeysDao.clearRemoteKeys()
					reportDao.clearAll()
				}

				val prevKey = if (offset == 0) null else offset - state.config.pageSize
				val nextKey = if (reportsDto.isEmpty()) null else offset + state.config.pageSize

				val keys = reportsDto.map {
					ReportRemoteKeysEntity(
						reportId = it.id,
						prevKey = prevKey,
						nextKey = nextKey
					)
				}

				remoteKeysDao.insertAll(keys)
				reportDao.insertAll(reportsDto.map { it.toEntity() })
			}

			MediatorResult.Success(endOfPaginationReached = reportsDto.isEmpty())
		} catch (e: IOException) {
			MediatorResult.Error(e)
		} catch (e: HttpException) {
			MediatorResult.Error(e)
		}
	}

	private suspend fun getRemoteKeyForLastItem(
		state: PagingState<Int, ReportEntity>
	): ReportRemoteKeysEntity? {
		return state.pages.lastOrNull { it.data.isNotEmpty() }
			?.data
			?.lastOrNull()
			?.let { report -> remoteKeysDao.remoteKeysReportId(report.id) }
	}

	private suspend fun fetchReports(offset: Int, limit: Int) =
		if (query.latitude != null && query.longitude != null) {
			val response = apiService.getReportsNearby(
				latitude = query.latitude,
				longitude = query.longitude,
				radiusKm = query.radiusKm,
				sort = query.sort.toApiSort(),
				limit = limit,
				offset = offset
			)

			if (!response.isSuccessful) {
				throw HttpException(response)
			}

			response.body().orEmpty()
		} else {
			apiService.getAllReports(offset = offset, limit = limit)
		}

	private fun FeedSort.toApiSort(): String {
		return when (this) {
			FeedSort.NEARBY -> "nearby"
			FeedSort.RELEVANT -> "relevant"
			FeedSort.RECENT -> "recent"
		}
	}
}

