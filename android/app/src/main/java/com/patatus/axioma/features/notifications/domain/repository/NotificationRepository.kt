package com.patatus.axioma.features.notifications.domain.repository


import androidx.paging.PagingData
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.domain.entities.NotificationRealTimeEvent
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<PagingData<NotificationEntity>>
    fun observeRealtimeEvents(): Flow<NotificationRealTimeEvent>
    suspend fun applyRealtimeEvent(event: NotificationRealTimeEvent)
    suspend fun saveIncomingPushNotification(
        id: Int?,
        title: String,
        body: String,
        type: String,
        referenceId: Int?,
        createdAt: String,
    )
    suspend fun markNotificationAsRead(notificationId: Int): Result<Unit>
}