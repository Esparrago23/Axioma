package com.patatus.axioma.features.reports.presentation.screens

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    object Success : ReportUiState()
    data class Error(val msg: String) : ReportUiState()
}