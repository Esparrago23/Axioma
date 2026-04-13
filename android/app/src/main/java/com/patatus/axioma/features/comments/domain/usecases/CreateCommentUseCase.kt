package com.patatus.axioma.features.comments.domain.usecases

import com.patatus.axioma.features.comments.domain.entities.Comment
import com.patatus.axioma.features.comments.domain.repositories.CommentsRepository
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(private val repo: CommentsRepository) {
    suspend operator fun invoke(reportId: Int, content: String): Result<Comment> =
        repo.createComment(reportId, content)
}
