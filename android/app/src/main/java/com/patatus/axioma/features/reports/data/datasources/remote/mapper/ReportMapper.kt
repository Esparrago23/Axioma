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
        userId = this.userId,
        createdAt = this.createdAt,
        userVote = this.userVote ?: 0
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
        authorId = this.userId,
        userVote = this.userVote
    )
}

fun Report.toEntity(userVote: Int = this.userVote): ReportEntity {
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
        userId = this.authorId,
        createdAt = this.createdAt,
        userVote = userVote
    )
}