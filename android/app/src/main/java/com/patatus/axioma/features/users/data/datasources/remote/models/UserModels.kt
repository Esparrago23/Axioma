package com.patatus.axioma.features.users.data.datasources.remote.models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("reputation_score") val reputationScore: Int,
    @SerializedName("profile_picture_url") val profilePictureUrl: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("created_at") val createdAt: String
)

data class UserUpdateRequest(
    val username: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("profile_picture_url") val profilePictureUrl: String? = null
)
