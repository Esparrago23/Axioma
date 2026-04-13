package com.patatus.axioma.features.reports.domain.entities

data class ReportEvolution(
    val id: Int,
    val reportId: Int,
    val userId: Int,
    val type: String,
    val description: String,
    val photoUrl: String?,
    val credibilityScore: Int,
    val status: String,
    val isValid: Boolean,
    val userVote: Int,
    val userLatitude: Double,
    val userLongitude: Double,
    val createdAt: String,
)
