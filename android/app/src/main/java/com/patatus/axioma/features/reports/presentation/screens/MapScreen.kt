package com.patatus.axioma.features.reports.presentation.screens

import android.R
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.mapbox.maps.CameraOptions
import com.patatus.axioma.core.hardware.location.LocationCapture
import com.patatus.axioma.core.hardware.location.LocationPermissionStatus
import com.patatus.axioma.core.hardware.location.LocationResult2
import com.patatus.axioma.core.hardware.location.rememberLocationPermissionState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.patatus.axioma.core.hardware.location.rememberLocationPermissionState
import kotlinx.coroutines.delay

@Composable
fun MapScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val mapReports by viewModel.mapReports.collectAsState()
    val feedQuery by viewModel.feedQuery.collectAsState()

    val permissionState = rememberLocationPermissionState()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(11.0)
            center(Point.fromLngLat(-99.1332, 19.4326))
        }
    }

    val reportMarkerBitmap = remember {
        ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)!!.toBitmap()
    }

    val currentLocationBitmap = remember {
        ContextCompat.getDrawable(context, android.R.drawable.presence_online)!!.toBitmap()
    }


    LaunchedEffect(Unit) {
        if (permissionState.status != LocationPermissionStatus.GRANTED) {
            permissionState.requestPermission()
        }
    }

    LaunchedEffect(permissionState.status) {
        android.util.Log.d("MapScreen", "status = ${permissionState.status}")
        if (permissionState.status == LocationPermissionStatus.GRANTED) {
            android.util.Log.d("MapScreen", "obteniendo ubicacion...")
            val result = LocationCapture(context).getCurrentLocation()
            android.util.Log.d("MapScreen", "resultado = $result")
            if (result is LocationResult2.Success) {
                val coords = result.coordinates
                viewModel.onLocationUpdated(coords.latitude, coords.longitude)
                delay(5000) // esperar a que el mapa esté listo
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
            feedQuery.latitude?.let { lat ->
                feedQuery.longitude?.let { lng ->
                    PointAnnotation(
                        point = Point.fromLngLat(lng, lat)
                    ){
                        iconImage = IconImage(currentLocationBitmap)
                    }
                }
            }
            mapReports.forEach { report ->
                PointAnnotation(
                    point = Point.fromLngLat(report.longitude, report.latitude)
                ) {
                    iconImage = IconImage(reportMarkerBitmap)
                }
            }
        }
    }
}
