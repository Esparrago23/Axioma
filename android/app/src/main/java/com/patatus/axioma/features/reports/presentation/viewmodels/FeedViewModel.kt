package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.usecases.GetReportsFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getReportsFeedUseCase: GetReportsFeedUseCase
) : ViewModel() {

    val reportsFeed: Flow<PagingData<Report>> = getReportsFeedUseCase()
        .cachedIn(viewModelScope)
}