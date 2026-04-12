package com.patatus.axioma.features.comments.domain.usecases

import com.patatus.axioma.features.comments.domain.repositories.CommentsRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(private val repo: CommentsRepository) {
    suspend operator fun invoke(reportId: Int, commentId: Int): Result<Boolean> =
        repo.deleteComment(reportId, commentId)
}
