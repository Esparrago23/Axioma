package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.patatus.axioma.features.reports.domain.usecases.CreateReportUseCase

class CreateReportViewModelFactory(
    private val createReportUseCase: CreateReportUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateReportViewModel::class.java)) {
            return CreateReportViewModel(createReportUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}