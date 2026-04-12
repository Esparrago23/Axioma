package com.patatus.axioma.features.comments.domain.entities

data class Comment(
    val id: Int,
    val reportId: Int,
    val userId: Int,
    val content: String,
    val createdAt: String,
)
