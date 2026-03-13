package com.patatus.axioma.features.users.data.datasources.remote.api

import com.patatus.axioma.features.users.data.datasources.remote.models.UserResponse
import com.patatus.axioma.features.users.data.datasources.remote.models.UpdateFcmTokenRequest
import com.patatus.axioma.features.users.data.datasources.remote.models.UserUpdateRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface UsersApiService {
    @GET("users/me")
    suspend fun getMyProfile(): UserResponse

    @PATCH("users/me")
    suspend fun updateMyProfile(@Body request: UserUpdateRequest): UserResponse

    @PATCH("users/me/fcm-token")
    suspend fun updateMyFcmToken(@Body request: UpdateFcmTokenRequest): Response<Unit>

    @Multipart
    @POST("users/me/photo")
    suspend fun uploadMyProfilePhoto(
        @Part photo: MultipartBody.Part
    ): UserResponse

    @DELETE("users/me")
    suspend fun deleteMyAccount(): Response<Unit>
}
