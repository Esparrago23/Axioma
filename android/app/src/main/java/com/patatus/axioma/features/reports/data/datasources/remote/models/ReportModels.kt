package com.patatus.axioma.features.reports.data.datasources.remote.models

import com.google.gson.annotations.SerializedName


data class ReportCreateRequest(
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("photo_url") val photoUrl: String? = null
)


data class ReportResponse(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("credibility_score") val credibilityScore: Int,
    val status: String,
    @SerializedName("created_at") val createdAt: String
)