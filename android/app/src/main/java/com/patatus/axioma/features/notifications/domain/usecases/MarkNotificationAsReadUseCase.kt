package com.patatus.axioma.features.notifications.domain.usecases

import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: Int): Result<Unit> {
        return repository.markNotificationAsRead(notificationId)
    }
}
