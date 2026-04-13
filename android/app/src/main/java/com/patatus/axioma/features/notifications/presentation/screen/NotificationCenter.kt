package com.patatus.axioma.features.notifications.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.presentation.components.NotificationCard
import com.patatus.axioma.features.notifications.presentation.viewmodels.NotificationViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenter(
    viewModel: NotificationViewmodel = hiltViewModel(),
    onNavigateToProfile: () -> Unit
) {
    val notifications: LazyPagingItems<NotificationEntity> =
        viewModel.notifications.collectAsLazyPagingItems()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avisos") },
                actions = {
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
                                contentDescription = "Ir al Perfil",
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
                                    contentDescription = "Ir al Perfil",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        // ⭐ NUEVO: LÓGICA DE PULL TO REFRESH (1.3.0) ⭐
        val isRefreshing = notifications.loadState.refresh is LoadState.Loading

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { notifications.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Evaluamos los estados de Paging 3
            when {
                notifications.loadState.refresh is LoadState.Error -> {
                    Text(
                        text = "Error al cargar notificaciones",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // Si no hay items y NO está cargando, mostramos texto vacío
                notifications.itemCount == 0 && !isRefreshing -> {
                    Text(
                        text = "Sin notificaciones",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(notifications.itemCount) { index ->
                            notifications[index]?.let { notification ->
                                NotificationCard(notification = notification)
                            }
                        }

                        // Loader para cuando scrolleas hacia abajo (paginación)
                        if (notifications.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}