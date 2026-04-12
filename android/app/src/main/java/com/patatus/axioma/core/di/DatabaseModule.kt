package com.patatus.axioma.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.core.database.AxiomaDatabase
import com.patatus.axioma.features.notifications.data.datasources.local.daos.NotificationDao
import com.patatus.axioma.features.notifications.data.datasources.local.daos.NotificationRemoteKeysDao
import com.patatus.axioma.features.reports.data.datasources.local.db.daos.ReportDao
import com.patatus.axioma.features.reports.data.datasources.local.db.daos.ReportRemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE reports ADD COLUMN user_vote INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `notifications` (
                    `id` INTEGER NOT NULL,
                    `title` TEXT NOT NULL,
                    `body` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `referenceId` INTEGER,
                    `createdAt` TEXT NOT NULL,
                    `isRead` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )"""
            )
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `notification_remote_keys` (
                    `notificationId` INTEGER NOT NULL,
                    `prevKey` INTEGER,
                    `nextKey` INTEGER,
                    PRIMARY KEY(`notificationId`)
                )"""
            )
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_created_at` ON `reports` (`created_at`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_credibility_score` ON `reports` (`credibility_score`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_user_id` ON `reports` (`user_id`)")
        }
    }

    @Provides
    @Singleton
    fun provideAxiomaDatabase(
        @ApplicationContext context: Context
    ): AxiomaDatabase {
        val builder = Room.databaseBuilder(
            context,
            AxiomaDatabase::class.java,
            "axioma_database"
        ).addMigrations(migration1To2, migration2To3, migration3To4)

        return if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration().build()
        } else {
            builder.build()
        }
    }

    @Provides
    @Singleton
    fun provideReportDao(database: AxiomaDatabase): ReportDao {
        return database.reportDao()
    }

    @Provides
    @Singleton
    fun provideReportRemoteKeysDao(database: AxiomaDatabase): ReportRemoteKeysDao {
        return database.reportRemoteKeysDao()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: AxiomaDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideNotificationRemoteKeysDao(database: AxiomaDatabase): NotificationRemoteKeysDao {
        return database.notificationRemoteKeysDao()
    }
}