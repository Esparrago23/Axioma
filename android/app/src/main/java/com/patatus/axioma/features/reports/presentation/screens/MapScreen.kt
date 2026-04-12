package com.patatus.axioma.features.reports.presentation.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.R
import com.patatus.axioma.core.hardware.location.LocationCapture
import com.patatus.axioma.core.hardware.location.LocationPermissionStatus
import com.patatus.axioma.core.hardware.location.LocationResult2
import com.patatus.axioma.core.hardware.location.rememberLocationPermissionState
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.presentation.viewmodels.FeedViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapReports by viewModel.mapReports.collectAsStateWithLifecycle()
    val feedQuery by viewModel.feedQuery.collectAsStateWithLifecycle()
    val mapCategory by viewModel.mapCategory.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val categories = stringArrayResource(R.array.report_categories).toList()

    var sliderRadius by remember { mutableFloatStateOf(feedQuery.radiusKm.toFloat()) }
    var selectedGroup by remember { mutableStateOf<List<Report>?>(null) }
    val iconScale: Double = when {
        sliderRadius <= 10f -> 1.5
        sliderRadius <= 20f -> 1.2
        else -> 1.0
    }
    val profileImageUrl = remember(userProfile?.profilePicture) {
        val rawUrl = userProfile?.profilePicture ?: ""
        when {
            rawUrl.isBlank() -> null
            Uri.parse(rawUrl).host != null -> rawUrl
            else -> "${BuildConfig.BASE_URL_API.removeSuffix("/")}/${rawUrl.removePrefix("/")}"
        }
    }

    val permissionState = rememberLocationPermissionState()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(11.0)
            center(Point.fromLngLat(-99.1332, 19.4326))
        }
    }

    val defaultUserBitmap = remember {
        val size = 64
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)!!.mutate()
        drawable.setTint(android.graphics.Color.parseColor("#2196F3"))
        drawable.setBounds(0, 0, size, size)
        drawable.draw(Canvas(bmp))
        bmp
    }

    var userMarkerBitmap by remember { mutableStateOf(defaultUserBitmap) }

    LaunchedEffect(profileImageUrl) {
        if (profileImageUrl == null) return@LaunchedEffect
        try {
            val request = ImageRequest.Builder(context)
                .data(profileImageUrl)
                .allowHardware(false)
                .size(128, 128)
                .build()
            val imageResult = ImageLoader(context).execute(request)
            if (imageResult is SuccessResult) {
                userMarkerBitmap = circularBitmap(imageResult.drawable.toBitmap(), 128)
            }
        } catch (_: Exception) { }
    }

    val singleReportBitmap = remember {
        val size = 64
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)!!.mutate()
        drawable.setTint(android.graphics.Color.RED)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(Canvas(bmp))
        bmp
    }

    val clusterBitmapCache = remember { mutableMapOf<Int, Bitmap>() }

    fun clusterBitmap(count: Int): Bitmap = clusterBitmapCache.getOrPut(count) {
        val size = 80
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.RED }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bg)
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, border)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = if (count > 9) 26f else 30f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("$count", size / 2f, size / 2f + 10f, text)
        bmp
    }

    val groupedReports = remember(mapReports) {
        mapReports.groupBy { report ->
            Pair(
                (report.latitude * 10000).roundToInt(),
                (report.longitude * 10000).roundToInt()
            )
        }
    }

    LaunchedEffect(Unit) {
        if (permissionState.status != LocationPermissionStatus.GRANTED) {
            permissionState.requestPermission()
        }
    }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status == LocationPermissionStatus.GRANTED) {
            val locationResult = LocationCapture(context).getCurrentLocation()
            if (locationResult is LocationResult2.Success) {
                val coords = locationResult.coordinates
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

    selectedGroup?.let { group ->
        ModalBottomSheet(
            onDismissRequest = { selectedGroup = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Text(
                text = "${group.size} reportes en este lugar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider()
            LazyColumn {
                items(group) { report ->
                    ListItem(
                        headlineContent = { Text(report.title) },
                        supportingContent = { Text(report.category) },
                        modifier = Modifier.clickable {
                            selectedGroup = null
                            onNavigateToDetail(report.id)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        ) {
            groupedReports.forEach { (_, group) ->
                val first = group.first()
                val markerBitmap = if (group.size == 1) singleReportBitmap else clusterBitmap(group.size)
                PointAnnotation(
                    point = Point.fromLngLat(first.longitude, first.latitude),
                    onClick = {
                        if (group.size == 1) {
                            onNavigateToDetail(first.id)
                        } else {
                            selectedGroup = group
                        }
                        true
                    }
                ) {
                    iconImage = IconImage(markerBitmap)
                    iconSize = iconScale
                }
            }

            feedQuery.latitude?.let { lat ->
                feedQuery.longitude?.let { lng ->
                    PointAnnotation(point = Point.fromLngLat(lng, lat)) {
                        iconImage = IconImage(userMarkerBitmap)
                        iconSize = iconScale
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Radio: ${sliderRadius.toInt()} km",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = sliderRadius,
                    onValueChange = { sliderRadius = it },
                    onValueChangeFinished = {
                        feedQuery.latitude?.let { lat ->
                            feedQuery.longitude?.let { lng ->
                                viewModel.onLocationUpdated(lat, lng, sliderRadius.toInt())
                            }
                        }
                    },
                    valueRange = 5f..50f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = mapCategory == null,
                            onClick = { viewModel.onMapCategorySelected(null) },
                            label = { Text("Todas") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = mapCategory == cat,
                            onClick = { viewModel.onMapCategorySelected(cat) },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                feedQuery.latitude?.let { lat ->
                    feedQuery.longitude?.let { lng ->
                        scope.launch {
                            mapViewportState.flyTo(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(lng, lat))
                                    .zoom(14.0)
                                    .build()
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
        }

        if (feedQuery.latitude != null && mapReports.isEmpty()) {
            Text(
                text = "Sin reportes en tu área",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp)
            )
        }
    }
}

private fun circularBitmap(source: Bitmap, size: Int): Bitmap {
    val squareSize = minOf(source.width, source.height)
    val xOffset = (source.width - squareSize) / 2
    val yOffset = (source.height - squareSize) / 2
    val squared = Bitmap.createBitmap(source, xOffset, yOffset, squareSize, squareSize)

    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    val scaled = Bitmap.createScaledBitmap(squared, size, size, true)
    canvas.drawBitmap(scaled, 0f, 0f, paint)
    if (squared != source) squared.recycle()
    return output
}
