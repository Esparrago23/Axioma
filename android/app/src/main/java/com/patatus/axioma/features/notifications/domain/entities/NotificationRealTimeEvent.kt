package com.patatus.axioma.features.notifications.domain.entities

sealed interface NotificationRealTimeEvent {
    data class NewNotification(val notification: NotificationEntity) : NotificationRealTimeEvent
}