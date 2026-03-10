package com.patatus.axioma.features.reports.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.usecases.*
import com.patatus.axioma.features.users.domain.usecases.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val report: Report, val currentUserId: Int) : DetailUiState()
    data class Error(val msg: String) : DetailUiState()
    object Deleted : DetailUiState()
}

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val getReportDetailUseCase: GetReportDetailUseCase,
    private val voteReportUseCase: VoteReportUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val updateReportUseCase: UpdateReportUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val uploadReportPhotoUseCase: UploadReportPhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadReport(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val reportResult = getReportDetailUseCase(id)
            val userResult = getUserProfileUseCase()

            if (reportResult.isSuccess && userResult.isSuccess) {
                _uiState.value = DetailUiState.Success(
                    report = reportResult.getOrThrow(),
                    currentUserId = userResult.getOrThrow().id
                )
            } else {
                _uiState.value = DetailUiState.Error("Error de sincronización")
            }
        }
    }

    fun toggleVote(isUpvote: Boolean) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        val report = state.report
        val newVoteValue = if (isUpvote) 1 else -1
        val finalVote = if (report.userVote == newVoteValue) 0 else newVoteValue
        val diff = finalVote - report.userVote

        val optimisticReport = report.copy(
            userVote = finalVote,
            credibilityScore = report.credibilityScore + diff
        )
        _uiState.value = state.copy(report = optimisticReport)

        viewModelScope.launch {
            voteReportUseCase(report.id, isUpvote)
                .onFailure { _uiState.value = state }
        }
    }

    fun updateReportWithMedia(id: Int, title: String, desc: String, photoUri: String?, deletePhoto: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value as? DetailUiState.Success ?: return@launch
            _uiState.value = DetailUiState.Loading

            try {
                var finalPhotoUrl: String? = currentState.report.photoUrl

                if (photoUri != null && (photoUri.startsWith("content://") || photoUri.startsWith("file://"))) {
                    val uploadResult = uploadReportPhotoUseCase(photoUri)
                    finalPhotoUrl = uploadResult.getOrNull()

                    if (finalPhotoUrl == null) {
                        _uiState.value = DetailUiState.Error("Error al subir la imagen")
                        return@launch
                    }
                }
                else if (deletePhoto) {
                    finalPhotoUrl = null
                }

                updateReportUseCase(id, title, desc, finalPhotoUrl)
                    .onSuccess { loadReport(id) }
                    .onFailure { _uiState.value = DetailUiState.Error("Fallo al actualizar el reporte") }

            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            deleteReportUseCase(id)
                .onSuccess { _uiState.value = DetailUiState.Deleted }
                .onFailure { _uiState.value = DetailUiState.Error(it.message ?: "Error al borrar") }
        }
    }
}