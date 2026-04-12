package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.usecases.GetMyReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReportsViewModel @Inject constructor(
    private val getMyReportsUseCase: GetMyReportsUseCase
) : ViewModel() {

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

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
}