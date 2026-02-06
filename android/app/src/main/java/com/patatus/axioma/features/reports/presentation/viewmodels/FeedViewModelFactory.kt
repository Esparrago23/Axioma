package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.patatus.axioma.features.reports.domain.usecases.GetReportsFeedUseCase

class FeedViewModelFactory(
    private val getReportsFeedUseCase: GetReportsFeedUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            return FeedViewModel(getReportsFeedUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}