package com.patatus.axioma.features.comments.domain.repositories

import com.patatus.axioma.features.comments.domain.entities.Comment

interface CommentsRepository {
    suspend fun getComments(reportId: Int): Result<List<Comment>>
    suspend fun createComment(reportId: Int, content: String): Result<Comment>
    suspend fun deleteComment(reportId: Int, commentId: Int): Result<Boolean>
}
