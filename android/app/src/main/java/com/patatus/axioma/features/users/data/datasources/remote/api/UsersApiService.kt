package com.patatus.axioma.features.users.data.datasources.remote.api

import com.patatus.axioma.features.users.data.datasources.remote.models.UserResponse
import com.patatus.axioma.features.users.data.datasources.remote.models.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UsersApiService {
    @GET("users/me")
    suspend fun getMyProfile(): UserResponse

    @PATCH("users/me")
    suspend fun updateMyProfile(@Body request: UserUpdateRequest): UserResponse

    @DELETE("users/me")
    suspend fun deleteMyAccount(): Response<Unit>
}
