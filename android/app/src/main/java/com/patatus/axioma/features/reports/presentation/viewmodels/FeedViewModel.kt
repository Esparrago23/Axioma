package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.usecases.GetReportsFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val reports: List<Report>) : FeedUiState()
    data class Error(val msg: String) : FeedUiState()
}

class FeedViewModel(
    private val getReportsFeedUseCase: GetReportsFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            getReportsFeedUseCase(offset = 0)
                .onSuccess { reports ->
                    _uiState.value = FeedUiState.Success(reports)
                }
                .onFailure { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Error al cargar el feed")
                }
        }
    }

    fun refresh() {
        loadReports()
    }
}