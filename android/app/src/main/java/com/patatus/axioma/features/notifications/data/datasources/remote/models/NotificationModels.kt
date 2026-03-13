package com.patatus.axioma.features.notifications.data.datasources.remote.models

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: Int,
    val title: String,
    val body: String,
    val type: String,
    @SerializedName("reference_id") val referenceId: Int?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_read") val isRead: Boolean
)