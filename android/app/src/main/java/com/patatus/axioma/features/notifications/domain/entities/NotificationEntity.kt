package com.patatus.axioma.features.notifications.domain.entities


data class NotificationEntity(
    val id: Int,
    val title: String,
    val body: String,
    val type: String,
    val referenceId: Int?,
    val createdAt: String,
    val isRead: Boolean
)