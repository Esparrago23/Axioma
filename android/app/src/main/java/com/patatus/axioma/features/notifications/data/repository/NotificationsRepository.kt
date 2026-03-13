package com.patatus.axioma.features.notifications.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.notifications.data.datasources.local.entities.NotificationEntity as NotificationRoomEntity
import com.patatus.axioma.features.notifications.data.datasources.remote.api.NotificationApiService
import com.patatus.axioma.features.notifications.data.datasources.remote.mapper.toDomain
import com.patatus.axioma.features.notifications.data.datasources.remote.mapper.toEntity
import com.patatus.axioma.features.notifications.data.datasources.remote.mediator.NotificationRemoteMediator
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.domain.entities.NotificationRealTimeEvent
import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val api: NotificationApiService,
    private val database: AxiomaDatabase,
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
        // El backend no expone un endpoint WebSocket para notificaciones.
        // Las notificaciones en tiempo real llegan vía FCM push →
        // IncomingPushNotificationHandler → Room. No se necesita WS aquí.
        return emptyFlow()
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

    override suspend fun saveIncomingPushNotification(
        id: Int?,
        title: String,
        body: String,
        type: String,
        referenceId: Int?,
        createdAt: String,
    ) {
        withContext(Dispatchers.IO) {
            val effectiveId = id ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            database.notificationDao().insert(
                NotificationRoomEntity(
                    id = effectiveId,
                    title = title,
                    body = body,
                    type = type,
                    referenceId = referenceId,
                    createdAt = createdAt,
                    isRead = false
                )
            )
        }
    }

    override suspend fun markNotificationAsRead(notificationId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                database.notificationDao().markAsRead(notificationId)

                val response = api.markNotificationAsRead(notificationId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(HttpException(response))
                }
            } catch (error: Throwable) {
                Result.failure(error)
            }
        }
    }
}