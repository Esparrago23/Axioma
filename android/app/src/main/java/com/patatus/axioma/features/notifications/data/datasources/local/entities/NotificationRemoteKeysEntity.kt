package com.patatus.axioma.features.notifications.data.datasources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_remote_keys")
data class NotificationRemoteKeysEntity(
    @PrimaryKey val notificationId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)