package com.patatus.axioma.features.reports.data.datasources.remote.mapper

import com.patatus.axioma.features.reports.data.datasources.remote.models.ReportResponse
import com.patatus.axioma.features.reports.domain.entities.Report

fun ReportResponse.toDomain(): Report {
    return Report(
        id = this.id,
        title = this.title,
        description = this.description,
        category = this.category,
        latitude = this.latitude,
        longitude = this.longitude,
        photoUrl = this.photoUrl,
        credibilityScore = this.credibilityScore,
        status = this.status,
        createdAt = this.createdAt
    )
}