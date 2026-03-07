package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.reports.domain.usecases.CreateReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.UploadReportPhotoUseCase
import com.patatus.axioma.features.reports.presentation.screens.ReportUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val createReportUseCase: CreateReportUseCase,
    private val uploadReportPhotoUseCase: UploadReportPhotoUseCase
) : ViewModel() {

    // Inputs
    var title = MutableStateFlow("")
    var description = MutableStateFlow("")
    var category = MutableStateFlow("INFRAESTRUCTURA")
    var latitude = MutableStateFlow(16.75)
    var longitude = MutableStateFlow(-93.11)
    var evidencePhotoUri = MutableStateFlow("")

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun sendReport() {
        if (title.value.isBlank() || description.value.isBlank()) {
            _uiState.value = ReportUiState.Error("Ponle título y descripción.")
            return
        }
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            val localPhoto = evidencePhotoUri.value.trim()

            val photoUrlResult = if (localPhoto.startsWith("content://") || localPhoto.startsWith("file://")) {
                uploadReportPhotoUseCase(localPhoto)
            } else {
                Result.success(localPhoto.ifBlank { null })
            }

            photoUrlResult.onFailure {
                _uiState.value = ReportUiState.Error(it.message ?: "No se pudo subir la evidencia")
                return@launch
            }

            val uploadedPhotoUrl = photoUrlResult.getOrNull()

            val result = createReportUseCase(
                title = title.value,
                desc = description.value,
                lat = latitude.value,
                long = longitude.value,
                cat = category.value,
                photoUrl = uploadedPhotoUrl
            )

            result.onSuccess {
                _uiState.value = ReportUiState.Success
                title.value = ""
                description.value = ""
                category.value = "INFRAESTRUCTURA"
                evidencePhotoUri.value = ""
            }.onFailure {
                _uiState.value = ReportUiState.Error(it.message ?: "Error al enviar")
            }
        }
    }

    fun onEvidencePhotoSelected(uri: String) {
        evidencePhotoUri.value = uri
    }

    fun resetState() { _uiState.value = ReportUiState.Idle }
}