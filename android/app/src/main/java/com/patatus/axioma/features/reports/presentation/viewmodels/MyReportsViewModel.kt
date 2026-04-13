package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.usecases.GetMyReportsUseCase
// IMPORTACIONES NUEVAS PARA EL PERFIL
import com.patatus.axioma.features.users.domain.entities.User
import com.patatus.axioma.features.users.domain.usecases.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReportsViewModel @Inject constructor(
    private val getMyReportsUseCase: GetMyReportsUseCase,
    // 1. Inyectamos el UseCase del perfil
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // 2. Estado para el perfil del usuario
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadMyReports() // Carga inicial vacía
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce de medio segundo para no saturar la API
            loadMyReports(query.ifBlank { null })
        }
    }

    fun refreshReports() {
        loadMyReports(_searchQuery.value.ifBlank { null })
    }

    fun consumeError() { _errorMessage.value = null }

    private fun loadMyReports(query: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            getMyReportsUseCase(query)
                .onSuccess {
                    _reports.value = it
                    _errorMessage.value = null
                }
                .onFailure {
                    _reports.value = emptyList()
                    _errorMessage.value = it.message ?: "No se pudieron cargar los reportes"
                }
            _isLoading.value = false
        }
    }

    // 3. Función para cargar el perfil (usando getOrNull como vimos antes)
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val result = getUserProfileUseCase()
                _userProfile.value = result.getOrNull()
            } catch (e: Exception) {
                android.util.Log.e("MyReportsVM", "Error cargando perfil: ${e.message}")
            }
        }
    }
}