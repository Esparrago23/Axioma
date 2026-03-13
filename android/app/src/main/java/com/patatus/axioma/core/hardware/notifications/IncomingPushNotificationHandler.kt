package com.patatus.axioma.core.hardware.notifications

import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomingPushNotificationHandler @Inject constructor(
    private val notificationsRepository: NotificationRepository
) {
    suspend fun handleIncomingMessage(
        id: Int?,
        title: String,
        body: String,
        type: String,
        referenceId: Int?,
        createdAt: String?
    ) {
        notificationsRepository.saveIncomingPushNotification(
            id = id,
            title = title,
            body = body,
            type = type,
            referenceId = referenceId,
            createdAt = createdAt ?: Instant.now().toString()
        )
    }
}
