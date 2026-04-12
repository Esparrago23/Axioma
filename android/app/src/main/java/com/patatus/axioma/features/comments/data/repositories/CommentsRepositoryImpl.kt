package com.patatus.axioma.features.comments.data.repositories

import com.patatus.axioma.features.comments.data.remote.api.CommentsApiService
import com.patatus.axioma.features.comments.data.remote.models.CreateCommentRequest
import com.patatus.axioma.features.comments.domain.entities.Comment
import com.patatus.axioma.features.comments.domain.repositories.CommentsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class CommentsRepositoryImpl @Inject constructor(
    private val api: CommentsApiService
) : CommentsRepository {

    override suspend fun getComments(reportId: Int): Result<List<Comment>> = safeCall {
        api.getComments(reportId).map {
            Comment(id = it.id, reportId = it.reportId, userId = it.userId, content = it.content, createdAt = it.createdAt)
        }
    }

    override suspend fun createComment(reportId: Int, content: String): Result<Comment> = safeCall {
        val r = api.createComment(reportId, CreateCommentRequest(content))
        Comment(id = r.id, reportId = r.reportId, userId = r.userId, content = r.content, createdAt = r.createdAt)
    }

    override suspend fun deleteComment(reportId: Int, commentId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteComment(reportId, commentId)
                if (response.isSuccessful) Result.success(true)
                else {
                    val msg = try { JSONObject(response.errorBody()?.string()).getString("detail") } catch (e: Exception) { "Error al eliminar" }
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: HttpException) {
            val msg = try { JSONObject(e.response()?.errorBody()?.string()).getString("detail") } catch (ex: Exception) { "Error del servidor" }
            Result.failure(Exception(msg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
