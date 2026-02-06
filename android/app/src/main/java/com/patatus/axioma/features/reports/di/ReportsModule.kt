package com.patatus.axioma.features.reports.di

import com.patatus.axioma.core.di.AppContainer
import com.patatus.axioma.features.reports.domain.usecases.CreateReportUseCase
import com.patatus.axioma.features.reports.presentation.viewmodels.CreateReportViewModelFactory

class ReportsModule(private val appContainer: AppContainer) {

    fun provideCreateReportViewModelFactory(): CreateReportViewModelFactory {
        return CreateReportViewModelFactory(
            CreateReportUseCase(appContainer.reportsRepository)
        )
    }
}