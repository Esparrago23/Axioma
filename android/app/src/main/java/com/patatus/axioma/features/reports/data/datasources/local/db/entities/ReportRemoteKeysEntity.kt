package com.patatus.axioma.features.reports.data.datasources.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_remote_keys")
data class ReportRemoteKeysEntity(
    @PrimaryKey val reportId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)