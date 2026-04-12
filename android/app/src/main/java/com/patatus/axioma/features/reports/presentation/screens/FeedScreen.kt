package com.patatus.axioma.features.reports.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.features.reports.domain.entities.FeedSort
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.presentation.viewmodels.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    currentLatitude: Double? = null,
    currentLongitude: Double? = null,
    cityRadiusKm: Int = 15
) {
    val feedQuery by viewModel.feedQuery.collectAsStateWithLifecycle()
    val reports = viewModel.reportsFeed.collectAsLazyPagingItems()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
         viewModel.loadUserProfile()
    }

    LaunchedEffect(currentLatitude, currentLongitude, cityRadiusKm) {
        if (currentLatitude != null && currentLongitude != null) {
            viewModel.onLocationUpdated(
                latitude = currentLatitude,
                longitude = currentLongitude,
                radiusKm = cityRadiusKm
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Axioma") },
                actions = {
                    // ⭐ NUEVO: Botón de perfil con imagen
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        val profileImageUrl = remember(userProfile?.profilePicture) {
                            val rawUrl = userProfile?.profilePicture ?: ""
                            when {
                                rawUrl.isBlank() -> null
                                rawUrl.startsWith("http") -> rawUrl.replace("localhost", "10.0.2.2")
                                else -> "${BuildConfig.BASE_URL_API.removeSuffix("/")}/${rawUrl.removePrefix("/")}"
                            }
                        }

                        if (profileImageUrl != null) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Perfil",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = if (feedQuery.sort == FeedSort.RELEVANT) 0 else 1) {
                Tab(
                    selected = feedQuery.sort == FeedSort.RELEVANT,
                    onClick = { viewModel.onSortSelected(FeedSort.RELEVANT) },
                    text = { Text("Relevantes") }
                )
                Tab(
                    selected = feedQuery.sort == FeedSort.RECENT,
                    onClick = { viewModel.onSortSelected(FeedSort.RECENT) },
                    text = { Text("Recientes") }
                )
            }

            when (val refreshState = reports.loadState.refresh) {
                is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is LoadState.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = refreshState.error.message ?: "Error al cargar el feed",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { reports.retry() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                is LoadState.NotLoading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            count = reports.itemCount,
                            key = reports.itemKey { it.id },
                            contentType = reports.itemContentType { "Report" }
                        ) { index ->
                            val report = reports[index]
                            if (report != null) {
                                ReportItem(report = report, onClick = { onNavigateToDetail(report.id) })
                            }
                        }

                        when (val appendState = reports.loadState.append) {
                            is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            is LoadState.Error -> {
                                item {
                                    Text(
                                        text = "Error al cargar mas denuncias: ${appendState.error.message}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            is LoadState.NotLoading -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = report.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ThumbUp, contentDescription = "Reputación", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${report.credibilityScore}", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = report.status, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}