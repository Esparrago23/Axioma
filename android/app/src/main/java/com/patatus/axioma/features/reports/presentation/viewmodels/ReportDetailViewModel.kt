package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.usecases.DeleteReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.GetReportDetailUseCase
import com.patatus.axioma.features.reports.domain.usecases.UpdateReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.VoteReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val report: Report) : DetailUiState()
    data class Error(val msg: String) : DetailUiState()
    object Deleted : DetailUiState()
}

class ReportDetailViewModel(
    private val getReportDetailUseCase: GetReportDetailUseCase,
    private val voteReportUseCase: VoteReportUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val updateReportUseCase: UpdateReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadReport(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            getReportDetailUseCase(id)
                .onSuccess { report -> _uiState.value = DetailUiState.Success(report) }
                .onFailure { _uiState.value = DetailUiState.Error(it.message ?: "No se pudo cargar el reporte") }
        }
    }

    fun vote(id: Int, isUpvote: Boolean) {
        viewModelScope.launch {
            voteReportUseCase(id, isUpvote)
                .onSuccess {
                    loadReport(id)
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error("Error al votar: ${error.message}")
                }
        }
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            deleteReportUseCase(id)
                .onSuccess {
                    _uiState.value = DetailUiState.Deleted
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(error.message ?: "No se pudo borrar el reporte")
                }
        }
    }

    fun updateReport(id: Int, newTitle: String, newDesc: String) {
        if (newTitle.isBlank() || newDesc.isBlank()) {
            _uiState.value = DetailUiState.Error("El título y la descripción no pueden estar vacíos")
            return
        }

        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading

            updateReportUseCase(id, newTitle, newDesc)
                .onSuccess { updatedReport ->
                    _uiState.value = DetailUiState.Success(updatedReport)
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(error.message ?: "Error al actualizar")
                }
        }
    }
}