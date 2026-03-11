package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.patatus.axioma.features.reports.domain.entities.FeedQuery
import com.patatus.axioma.features.reports.domain.entities.FeedSort
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.entities.ReportRealtimeEvent
import com.patatus.axioma.features.reports.domain.usecases.ApplyReportRealtimeEventUseCase
import com.patatus.axioma.features.reports.domain.usecases.GetReportsFeedUseCase
import com.patatus.axioma.features.reports.domain.usecases.ObserveReportRealtimeEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getReportsFeedUseCase: GetReportsFeedUseCase,
    private val observeReportRealtimeEventsUseCase: ObserveReportRealtimeEventsUseCase,
    private val applyReportRealtimeEventUseCase: ApplyReportRealtimeEventUseCase
) : ViewModel() {

    private val _feedQuery = MutableStateFlow(FeedQuery())
    val feedQuery = _feedQuery.asStateFlow()

    val reportsFeed: Flow<PagingData<Report>> = _feedQuery
        .flatMapLatest { query -> getReportsFeedUseCase(query) }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            observeReportRealtimeEventsUseCase().collect { event ->
                if (shouldApplyRealtimeEvent(event, _feedQuery.value)) {
                    applyReportRealtimeEventUseCase(event)
                }
            }
        }
    }

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

    private fun shouldApplyRealtimeEvent(event: ReportRealtimeEvent, query: FeedQuery): Boolean {
        return when (event) {
            is ReportRealtimeEvent.NewReport -> query.matches(event.report)
            is ReportRealtimeEvent.VoteUpdate -> true
        }
    }

    private fun FeedQuery.matches(report: Report): Boolean {
        val feedLatitude = latitude ?: return true
        val feedLongitude = longitude ?: return true
        return haversineDistanceKm(
            lat1 = feedLatitude,
            lon1 = feedLongitude,
            lat2 = report.latitude,
            lon2 = report.longitude
        ) <= radiusKm
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val originLat = Math.toRadians(lat1)
        val destinationLat = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) +
            cos(originLat) * cos(destinationLat) * sin(dLon / 2).pow(2)

        return 2 * earthRadiusKm * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}