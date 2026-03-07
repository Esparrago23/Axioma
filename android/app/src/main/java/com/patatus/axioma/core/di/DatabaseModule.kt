package com.patatus.axioma.core.di

import android.content.Context
import androidx.room.Room
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.core.database.AxiomaDatabase
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

    @Provides
    @Singleton
    fun provideAxiomaDatabase(
        @ApplicationContext context: Context
    ): AxiomaDatabase {
        val builder = Room.databaseBuilder(
            context,
            AxiomaDatabase::class.java,
            "axioma_database"
        )

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
}