package com.patatus.axioma.features.reports.di

import com.patatus.axioma.features.reports.domain.repositories.ReportsRepository
import com.patatus.axioma.features.reports.domain.usecases.CreateReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.DeleteReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.GetReportDetailUseCase
import com.patatus.axioma.features.reports.domain.usecases.GetReportsFeedUseCase
import com.patatus.axioma.features.reports.domain.usecases.UpdateReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.VoteReportUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportsModule {

    @Provides
    @Singleton
    fun provideCreateReportUseCase(repository: ReportsRepository): CreateReportUseCase {
        return CreateReportUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetReportsFeedUseCase(repository: ReportsRepository): GetReportsFeedUseCase {
        return GetReportsFeedUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetReportDetailUseCase(repository: ReportsRepository): GetReportDetailUseCase {
        return GetReportDetailUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideVoteReportUseCase(repository: ReportsRepository): VoteReportUseCase {
        return VoteReportUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteReportUseCase(repository: ReportsRepository): DeleteReportUseCase {
        return DeleteReportUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateReportUseCase(repository: ReportsRepository): UpdateReportUseCase {
        return UpdateReportUseCase(repository)
    }
}