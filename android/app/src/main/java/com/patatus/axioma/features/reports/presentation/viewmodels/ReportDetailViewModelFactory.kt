package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.patatus.axioma.features.reports.domain.usecases.DeleteReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.GetReportDetailUseCase
import com.patatus.axioma.features.reports.domain.usecases.UpdateReportUseCase
import com.patatus.axioma.features.reports.domain.usecases.VoteReportUseCase

class ReportDetailViewModelFactory(
    private val getReportDetailUseCase: GetReportDetailUseCase,
    private val voteReportUseCase: VoteReportUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val updateReportUseCase: UpdateReportUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportDetailViewModel::class.java)) {
            return ReportDetailViewModel(
                getReportDetailUseCase,
                voteReportUseCase,
                deleteReportUseCase,
                updateReportUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}