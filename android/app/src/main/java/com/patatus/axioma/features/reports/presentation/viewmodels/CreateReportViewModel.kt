package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.usecases.CreateReportUseCase
import com.patatus.axioma.features.reports.presentation.screens.ReportUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateReportViewModel(
    private val createReportUseCase: CreateReportUseCase
) : ViewModel() {

    // Inputs
    var title = MutableStateFlow("")
    var description = MutableStateFlow("")
    var category = MutableStateFlow("INFRAESTRUCTURA")
    var latitude = MutableStateFlow(16.75)
    var longitude = MutableStateFlow(-93.11)

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun sendReport() {
        if (title.value.isBlank() || description.value.isBlank()) {
            _uiState.value = ReportUiState.Error("Ponle título y descripción.")
            return
        }
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            val result = createReportUseCase(
                title.value,
                description.value,
                latitude.value,
                longitude.value,
                category.value
            )

            result.onSuccess {
                _uiState.value = ReportUiState.Success
                title.value = ""
                description.value = ""
                category.value = "INFRAESTRUCTURA"
            }.onFailure {
                _uiState.value = ReportUiState.Error(it.message ?: "Error al enviar")
            }
        }
    }
    fun resetState() { _uiState.value = ReportUiState.Idle }
}