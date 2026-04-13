package com.patatus.axioma.features.reports.data.datasources.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reports",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["credibility_score"]),
        Index(value = ["user_id"])
    ]
)
data class ReportEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "photo_url") val photoUrl: String?,
    @ColumnInfo(name = "credibility_score") val credibilityScore: Int,
    val status: String,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "user_vote") val userVote: Int,
    @ColumnInfo(name = "distance_km") val distanceKm: Double? = null
)