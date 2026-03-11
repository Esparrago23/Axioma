package com.patatus.axioma.features.reports.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.ComposeMapInitOptions
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.patatus.axioma.features.reports.presentation.viewmodels.FeedViewModel

@Composable
fun MapScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val reports = viewModel.reportsFeed.collectAsLazyPagingItems()

    Box(Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = rememberMapViewportState()
        ) {
            for (index in 0 until reports.itemCount) {
                val report = reports[index]
                if (report != null) {
                    PointAnnotation(
                        point = Point.fromLngLat(
                            report.longitude,
                            report.latitude
                        )
                    )
                }
            }
        }
    }
}
