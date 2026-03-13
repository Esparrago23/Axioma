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

    @Provides
    @Singleton
    fun provideAxiomaDatabase(
        @ApplicationContext context: Context
    ): AxiomaDatabase {
        val builder = Room.databaseBuilder(
            context,
            AxiomaDatabase::class.java,
            "axioma_database"
        ).addMigrations(migration1To2)

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