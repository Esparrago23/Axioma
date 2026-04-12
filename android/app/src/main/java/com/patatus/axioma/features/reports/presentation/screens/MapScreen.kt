package com.patatus.axioma.features.reports.presentation.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.patatus.axioma.core.hardware.location.LocationCapture
import com.patatus.axioma.core.hardware.location.LocationPermissionStatus
import com.patatus.axioma.core.hardware.location.LocationResult2
import com.patatus.axioma.core.hardware.location.rememberLocationPermissionState
import com.patatus.axioma.features.reports.presentation.viewmodels.FeedViewModel

@Composable
fun MapScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val mapReports by viewModel.mapReports.collectAsStateWithLifecycle()
    val feedQuery by viewModel.feedQuery.collectAsStateWithLifecycle()

    val permissionState = rememberLocationPermissionState()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(11.0)
            center(Point.fromLngLat(-99.1332, 19.4326))
        }
    }

    val reportMarkerBitmap = remember {
        val size = 64
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)!!.mutate()
        drawable.setTint(android.graphics.Color.RED)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(Canvas(bmp))
        bmp
    }

    val userLocationBitmap = remember {
        val size = 64
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)!!.mutate()
        drawable.setTint(android.graphics.Color.parseColor("#2196F3"))
        drawable.setBounds(0, 0, size, size)
        drawable.draw(Canvas(bmp))
        bmp
    }

    LaunchedEffect(Unit) {
        if (permissionState.status != LocationPermissionStatus.GRANTED) {
            permissionState.requestPermission()
        }
    }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status == LocationPermissionStatus.GRANTED) {
            val result = LocationCapture(context).getCurrentLocation()
            if (result is LocationResult2.Success) {
                val coords = result.coordinates
                viewModel.onLocationUpdated(coords.latitude, coords.longitude)
                mapViewportState.flyTo(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(coords.longitude, coords.latitude))
                        .zoom(11.0)
                        .build()
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        ) {
            mapReports.forEach { report ->
                PointAnnotation(
                    point = Point.fromLngLat(report.longitude, report.latitude),
                    onClick = {
                        onNavigateToDetail(report.id)
                        true
                    }
                ) {
                    iconImage = IconImage(reportMarkerBitmap)
                }
            }

            feedQuery.latitude?.let { lat ->
                feedQuery.longitude?.let { lng ->
                    PointAnnotation(
                        point = Point.fromLngLat(lng, lat)
                    ) {
                        iconImage = IconImage(userLocationBitmap)
                    }
                }
            }
        }

        if (feedQuery.latitude != null && mapReports.isEmpty()) {
            Text(
                text = "Sin reportes en tu área",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}
