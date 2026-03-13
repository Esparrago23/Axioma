package com.patatus.axioma.features.notifications.presentation.screen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.presentation.components.NotificationCard
import com.patatus.axioma.features.notifications.presentation.viewmodels.NotificationViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenter(
    viewModel: NotificationViewmodel = hiltViewModel()
) {
    val notifications: LazyPagingItems<NotificationEntity> =
        viewModel.notifications.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            notifications.loadState.refresh is LoadState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            notifications.loadState.refresh is LoadState.Error -> {
                Text(
                    text = "Error al cargar notificaciones",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            notifications.itemCount == 0 -> {
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