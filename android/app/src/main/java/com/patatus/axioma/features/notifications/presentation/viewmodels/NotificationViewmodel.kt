package com.patatus.axioma.features.notifications.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.domain.entities.NotificationRealTimeEvent
import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import com.patatus.axioma.features.notifications.domain.usecases.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewmodel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val repository: NotificationRepository
) : ViewModel() {

    val notifications: Flow<PagingData<NotificationEntity>> =
        getNotificationsUseCase().cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            android.util.Log.d("NotificationVM", "Iniciando observación de eventos realtime")
            repository.observeRealtimeEvents().collect { event ->
                android.util.Log.d("NotificationVM", "Evento recibido: $event")
                repository.applyRealtimeEvent(event)
                android.util.Log.d("NotificationVM", "Evento aplicado a Room")
            }
        }
    }
}