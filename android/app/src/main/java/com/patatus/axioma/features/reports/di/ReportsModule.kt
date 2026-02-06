package com.patatus.axioma.features.reports.di

import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.features.reports.domain.usecases.CreateReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.GetReportDetailUseCase
import com.patatus.axioma.features.reports.domain.usecases.GetReportsFeedUseCase
import com.patatus.axioma.features.reports.domain.usecases.VoteReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.UpdateReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.DeleteReportUseCase
import com.patatus.axioma.features.reports.presentation.viewmodels.CreateReportViewModelFactory
import com.patatus.axioma.features.reports.presentation.viewmodels.FeedViewModelFactory
import com.patatus.axioma.features.reports.presentation.viewmodels.ReportDetailViewModelFactory

class ReportsModule(private val appContainer: AppContainer) {

    private fun provideCreateReportUseCase() = CreateReportUseCase(appContainer.reportsRepository)
    private fun provideGetReportsFeedUseCase() = GetReportsFeedUseCase(appContainer.reportsRepository)
    private fun provideGetReportDetailUseCase() = GetReportDetailUseCase(appContainer.reportsRepository)
    private fun provideVoteReportUseCase() = VoteReportUseCase(appContainer.reportsRepository)
    private fun provideDeleteReportUseCase() = DeleteReportUseCase(appContainer.reportsRepository)
    private fun provideUpdateReportUseCase() = UpdateReportUseCase(appContainer.reportsRepository)

    fun provideCreateReportViewModelFactory(): CreateReportViewModelFactory {
        return CreateReportViewModelFactory(provideCreateReportUseCase())
    }


    fun provideFeedViewModelFactory(): FeedViewModelFactory {
        return FeedViewModelFactory(provideGetReportsFeedUseCase())
    }

    fun provideReportDetailViewModelFactory(): ReportDetailViewModelFactory {
        return ReportDetailViewModelFactory(
            provideGetReportDetailUseCase(),
            provideVoteReportUseCase(),
            provideDeleteReportUseCase(),
            provideUpdateReportUseCase()
        )
    }
}