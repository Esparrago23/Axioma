package com.patatus.axioma.features.notifications.data.datasources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val type: String,
    val referenceId: Int?,
    val createdAt: String,
    val isRead: Boolean
)