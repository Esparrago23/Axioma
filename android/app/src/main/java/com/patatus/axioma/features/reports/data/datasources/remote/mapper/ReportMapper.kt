package com.patatus.axioma.features.reports.data.datasources.remote.mapper

import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportEntity
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
        createdAt = this.createdAt,
        userVote = this.userVote ?: 0,
        authorId = this.userId
    )
}

fun ReportResponse.toEntity(): ReportEntity {
    return ReportEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        category = this.category,
        latitude = this.latitude,
        longitude = this.longitude,
        photoUrl = this.photoUrl,
        credibilityScore = this.credibilityScore,
        status = this.status,
        userId = 0,
        createdAt = this.createdAt
    )
}

fun ReportEntity.toDomain(): Report {
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
        createdAt = this.createdAt,
        authorId = 0,
        userVote = 0
    )
}