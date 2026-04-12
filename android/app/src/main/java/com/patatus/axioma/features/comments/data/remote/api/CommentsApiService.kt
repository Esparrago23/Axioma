package com.patatus.axioma.features.comments.data.remote.api

import com.patatus.axioma.features.comments.data.remote.models.CommentResponse
import com.patatus.axioma.features.comments.data.remote.models.CreateCommentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentsApiService {
    @GET("reports/{reportId}/comments/")
    suspend fun getComments(@Path("reportId") reportId: Int): List<CommentResponse>

    @POST("reports/{reportId}/comments/")
    suspend fun createComment(
        @Path("reportId") reportId: Int,
        @Body body: CreateCommentRequest
    ): CommentResponse

    @DELETE("reports/{reportId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("reportId") reportId: Int,
        @Path("commentId") commentId: Int
    ): Response<Unit>
}
