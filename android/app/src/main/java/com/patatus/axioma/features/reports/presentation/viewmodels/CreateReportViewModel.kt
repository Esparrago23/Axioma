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

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _category = MutableStateFlow("INFRAESTRUCTURA")
    val category = _category.asStateFlow()

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude = _longitude.asStateFlow()

    private val _evidencePhotoUri = MutableStateFlow("")
    val evidencePhotoUri = _evidencePhotoUri.asStateFlow()

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onTitleChanged(value: String) { _title.value = value }
    fun onDescriptionChanged(value: String) { _description.value = value }
    fun onCategoryChanged(value: String) { _category.value = value }
    fun onLocationChanged(lat: Double, lng: Double) {
        _latitude.value = lat
        _longitude.value = lng
    }

    fun sendReport() {
        if (_title.value.isBlank() || _description.value.isBlank()) {
            _uiState.value = ReportUiState.Error("Ponle título y descripción.")
            return
        }
        val lat = _latitude.value
        val lng = _longitude.value
        if (lat == null || lng == null) {
            _uiState.value = ReportUiState.Error("No se pudo obtener tu ubicación. Asegúrate de tener el GPS activo.")
            return
        }
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            val localPhoto = _evidencePhotoUri.value.trim()

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
                title = _title.value,
                desc = _description.value,
                lat = lat,
                long = lng,
                cat = _category.value,
                photoUrl = uploadedPhotoUrl
            )

            result.onSuccess {
                _uiState.value = ReportUiState.Success
                _title.value = ""
                _description.value = ""
                _category.value = "INFRAESTRUCTURA"
                _evidencePhotoUri.value = ""
            }.onFailure {
                _uiState.value = ReportUiState.Error(it.message ?: "Error al enviar")
            }
        }
    }

    fun onEvidencePhotoSelected(uri: String) {
        _evidencePhotoUri.value = uri
    }

    fun resetState() { _uiState.value = ReportUiState.Idle }
}