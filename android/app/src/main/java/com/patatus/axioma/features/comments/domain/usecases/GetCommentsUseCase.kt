package com.patatus.axioma.features.comments.domain.usecases

import com.patatus.axioma.features.comments.domain.entities.Comment
import com.patatus.axioma.features.comments.domain.repositories.CommentsRepository
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(private val repo: CommentsRepository) {
    suspend operator fun invoke(reportId: Int): Result<List<Comment>> = repo.getComments(reportId)
}
