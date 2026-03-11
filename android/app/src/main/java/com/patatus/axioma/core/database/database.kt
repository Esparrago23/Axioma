package com.patatus.axioma.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.patatus.axioma.features.reports.data.datasources.local.db.daos.ReportDao
import com.patatus.axioma.features.reports.data.datasources.local.db.daos.ReportRemoteKeysDao
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportEntity
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportRemoteKeysEntity

@Database(
    entities = [
        ReportEntity::class,
        ReportRemoteKeysEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AxiomaDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun reportRemoteKeysDao(): ReportRemoteKeysDao
}