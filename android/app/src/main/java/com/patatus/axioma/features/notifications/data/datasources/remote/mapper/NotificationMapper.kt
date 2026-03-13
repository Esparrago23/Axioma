package com.patatus.axioma.features.notifications.data.datasources.remote.mapper

import com.patatus.axioma.features.notifications.data.datasources.local.entities.NotificationEntity as NotificationRoomEntity
import com.patatus.axioma.features.notifications.data.datasources.remote.models.NotificationResponse
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity as NotificationDomain

fun NotificationResponse.toEntity(): NotificationRoomEntity = NotificationRoomEntity(
    id = id,
    title = title,
    body = body,
    type = type,
    referenceId = referenceId,
    createdAt = createdAt,
    isRead = isRead
)

fun NotificationRoomEntity.toDomain(): NotificationDomain = NotificationDomain(
    id = id,
    title = title,
    body = body,
    type = type,
    referenceId = referenceId,
    createdAt = createdAt,
    isRead = isRead
)

fun NotificationDomain.toEntity(): NotificationRoomEntity = NotificationRoomEntity(
    id = id,
    title = title,
    body = body,
    type = type,
    referenceId = referenceId,
    createdAt = createdAt,
    isRead = isRead
)