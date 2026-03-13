package com.patatus.axioma.features.notifications.data.datasources.remote.api

import com.patatus.axioma.features.notifications.data.datasources.remote.models.NotificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {

    @GET("notifications/")
    suspend fun getNotifications(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): List<NotificationResponse>

    @POST("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") id: Int
    ): Response<Unit>
}