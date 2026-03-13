package com.patatus.axioma.features.notifications.domain.usecases

import androidx.paging.PagingData
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<PagingData<NotificationEntity>> = repository.getNotifications()
}