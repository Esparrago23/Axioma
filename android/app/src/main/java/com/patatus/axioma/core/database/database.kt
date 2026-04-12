package com.patatus.axioma.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.patatus.axioma.features.notifications.data.datasources.local.daos.NotificationDao
import com.patatus.axioma.features.notifications.data.datasources.local.daos.NotificationRemoteKeysDao
import com.patatus.axioma.features.notifications.data.datasources.local.entities.NotificationEntity
import com.patatus.axioma.features.notifications.data.datasources.local.entities.NotificationRemoteKeysEntity
import com.patatus.axioma.features.reports.data.datasources.local.db.daos.ReportDao
import com.patatus.axioma.features.reports.data.datasources.local.db.daos.ReportRemoteKeysDao
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportEntity
import com.patatus.axioma.features.reports.data.datasources.local.db.entities.ReportRemoteKeysEntity

@Database(
    entities = [
        ReportEntity::class,
        ReportRemoteKeysEntity::class,
        NotificationEntity::class,
        NotificationRemoteKeysEntity::class,
    ],
    version = 4,
    exportSchema = true
)
abstract class AxiomaDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun reportRemoteKeysDao(): ReportRemoteKeysDao

    abstract fun notificationDao(): NotificationDao
    abstract fun notificationRemoteKeysDao(): NotificationRemoteKeysDao
}