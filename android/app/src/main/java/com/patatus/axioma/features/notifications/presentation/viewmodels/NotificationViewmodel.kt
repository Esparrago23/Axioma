package com.patatus.axioma.features.notifications.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.domain.repository.NotificationRepository
import com.patatus.axioma.features.notifications.domain.usecases.GetNotificationsUseCase
// IMPORTANTE: Asegúrate de importar tus clases reales de perfil
import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.usecases.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewmodel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val repository: NotificationRepository,
    // 1. Inyectamos el UseCase o Repository que usas para sacar los datos del usuario
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    // --- ESTADO DE NOTIFICACIONES ---
    val notifications: Flow<PagingData<NotificationEntity>> =
        getNotificationsUseCase().cachedIn(viewModelScope)

    // --- ESTADO DEL PERFIL (LO NUEVO) ---
    // 2. Creamos el StateFlow para guardar y emitir el perfil a la UI
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    init {
        // Tu lógica en tiempo real intacta
        viewModelScope.launch {
            android.util.Log.d("NotificationVM", "Iniciando observación de eventos realtime")
            repository.observeRealtimeEvents().collect { event ->
                android.util.Log.d("NotificationVM", "Evento recibido: $event")
                repository.applyRealtimeEvent(event)
                android.util.Log.d("NotificationVM", "Evento aplicado a Room")
            }
        }
    }

    // 3. Función para cargar el perfil que llamará la UI al iniciar (LaunchedEffect)
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val result = getUserProfileUseCase()

                // Extraemos el User del Result. Si falló, asignará null automáticamente.
                _userProfile.value = result.getOrNull()

                // Opcional: Loggear si el Result vino con un error interno
                result.onFailure { exception ->
                    android.util.Log.e("NotificationVM", "Error en el Result: ${exception.message}")
                }

            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Excepción de red/sistema: ${e.message}")
            }
        }
    }
}