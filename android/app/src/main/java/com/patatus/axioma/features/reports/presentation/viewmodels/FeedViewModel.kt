package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.FeedSort
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.usecases.GetReportsFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getReportsFeedUseCase: GetReportsFeedUseCase
) : ViewModel() {

    private val _feedQuery = MutableStateFlow(FeedQuery())
    val feedQuery = _feedQuery.asStateFlow()

    val reportsFeed: Flow<PagingData<Report>> = _feedQuery
        .distinctUntilChanged()
        .flatMapLatest { query -> getReportsFeedUseCase(query) }
        .cachedIn(viewModelScope)

    fun onSortSelected(sort: FeedSort) {
        _feedQuery.value = _feedQuery.value.copy(sort = sort)
    }

    fun onLocationUpdated(latitude: Double, longitude: Double, radiusKm: Int = 15) {
        _feedQuery.value = _feedQuery.value.copy(
            latitude = latitude,
            longitude = longitude,
            radiusKm = radiusKm
        )
    }
}