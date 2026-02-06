package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.usecases.CreateReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.patatus.axioma.features.reports.presentation.screens.ReportUiState



class CreateReportViewModel(
    private val createReportUseCase: CreateReportUseCase
) : ViewModel() {

    // Inputs
    var title = MutableStateFlow("")
    var description = MutableStateFlow("")
    var latitude = MutableStateFlow(16.75)
    var longitude = MutableStateFlow(-93.11)

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun sendReport() {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            val result = createReportUseCase(
                title.value,
                description.value,
                latitude.value,
                longitude.value,
                "INFRASTRUCTURE"
            )

            result.onSuccess {
                _uiState.value = ReportUiState.Success
            }.onFailure {
                _uiState.value = ReportUiState.Error(it.message ?: "Error")
            }
        }
    }
}