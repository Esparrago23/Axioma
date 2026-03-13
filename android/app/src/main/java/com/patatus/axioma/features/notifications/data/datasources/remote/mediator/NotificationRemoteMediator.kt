package com.patatus.axioma.features.notifications.data.datasources.remote.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.notifications.data.datasources.local.entities.NotificationEntity
import com.patatus.axioma.features.notifications.data.datasources.local.entities.NotificationRemoteKeysEntity
import com.patatus.axioma.features.notifications.data.datasources.remote.api.NotificationApiService
import com.patatus.axioma.features.notifications.data.datasources.remote.mapper.toEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class NotificationRemoteMediator(
    private val apiService: NotificationApiService,
    private val database: AxiomaDatabase
) : RemoteMediator<Int, NotificationEntity>() {

    private val notificationDao = database.notificationDao()
    private val remoteKeysDao = database.notificationRemoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NotificationEntity>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            val notificationsDto = apiService.getNotifications(
                offset = offset,
                limit = state.config.pageSize
            )

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearRemoteKeys()
                    notificationDao.clearAll()
                }

                val prevKey = if (offset == 0) null else offset - state.config.pageSize
                val nextKey = if (notificationsDto.isEmpty()) null else offset + state.config.pageSize

                val keys = notificationsDto.map {
                    NotificationRemoteKeysEntity(
                        notificationId = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }

                remoteKeysDao.insertAll(keys)
                notificationDao.insertAll(notificationsDto.map { it.toEntity() })
            }

            MediatorResult.Success(endOfPaginationReached = notificationsDto.isEmpty())
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, NotificationEntity>
    ): NotificationRemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data
            ?.lastOrNull()
            ?.let { notification -> remoteKeysDao.remoteKeysNotificationId(notification.id) }
    }
}