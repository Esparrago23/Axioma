package com.patatus.axioma.features.notifications.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.notifications.data.datasources.remote.api.NotificationApiService
import com.patatus.axioma.features.notifications.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.notifications.data.datasources.remote.mapper.toEntity
import com.patatus.axioma.features.notifications.data.datasources.remote.mediator.NotificationRemoteMediator
import com.patatus.axioma.features.notifications.data.realtime.NotificationsRealtimeWebSocketDataSource
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.domain.entities.NotificationRealTimeEvent
import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val api: NotificationApiService,
    private val database: AxiomaDatabase,
    private val realtimeDataSource: NotificationsRealtimeWebSocketDataSource
) : NotificationRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getNotifications(): Flow<PagingData<NotificationEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = NotificationRemoteMediator(
                apiService = api,
                database = database
            ),
            pagingSourceFactory = { database.notificationDao().pagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    override fun observeRealtimeEvents(): Flow<NotificationRealTimeEvent> {
        return realtimeDataSource.observeEvents()
    }

    override suspend fun applyRealtimeEvent(event: NotificationRealTimeEvent) {
        withContext(Dispatchers.IO) {
            when (event) {
                is NotificationRealTimeEvent.NewNotification -> {
                    database.notificationDao().insert(event.notification.toEntity())
                }
            }
        }
    }
}