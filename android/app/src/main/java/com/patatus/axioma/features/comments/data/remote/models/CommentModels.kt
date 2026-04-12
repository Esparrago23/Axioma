package com.patatus.axioma.features.comments.data.remote.models

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    val id: Int,
    @SerializedName("report_id") val reportId: Int,
    @SerializedName("user_id") val userId: Int,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
)

data class CreateCommentRequest(
    val content: String
)
