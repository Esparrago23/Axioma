package com.patatus.axioma.features.reports.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.features.comments.domain.entities.Comment
import com.patatus.axioma.features.comments.domain.usecases.CreateCommentUseCase
import com.patatus.axioma.features.comments.domain.usecases.DeleteCommentUseCase
import com.patatus.axioma.features.comments.domain.usecases.GetCommentsUseCase
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.entities.ReportEvolution
import com.patatus.axioma.features.reports.domain.entities.ReportRealtimeEvent
import com.patatus.axioma.features.reports.domain.usecases.*
import com.patatus.axioma.features.users.domain.usecases.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val report: Report,
        val currentUserId: Int,
        val evolutions: List<ReportEvolution> = emptyList(),
        val comments: List<Comment> = emptyList(),
        val evolutionsLoading: Boolean = false,
        val commentsLoading: Boolean = false,
    ) : DetailUiState()
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
    private val uploadReportPhotoUseCase: UploadReportPhotoUseCase,
    private val observeReportRealtimeEventsUseCase: ObserveReportRealtimeEventsUseCase,
    private val getEvolutionsUseCase: GetEvolutionsUseCase,
    private val createEvolutionUseCase: CreateEvolutionUseCase,
    private val voteEvolutionUseCase: VoteEvolutionUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _evolutionError = MutableStateFlow<String?>(null)
    val evolutionError = _evolutionError.asStateFlow()

    fun clearEvolutionError() { _evolutionError.value = null }

    private var currentReportId: Int? = null

    init {
        viewModelScope.launch {
            observeReportRealtimeEventsUseCase().collect(::handleRealtimeEvent)
        }
    }

    fun loadReport(id: Int) {
        currentReportId = id
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val reportResult = getReportDetailUseCase(id)
            val userResult = getUserProfileUseCase()

            if (reportResult.isSuccess && userResult.isSuccess) {
                _uiState.value = DetailUiState.Success(
                    report = reportResult.getOrThrow(),
                    currentUserId = userResult.getOrThrow().id,
                )
                loadEvolutions(id)
                loadComments(id)
            } else {
                _uiState.value = DetailUiState.Error("Error de sincronización")
            }
        }
    }

    private fun loadEvolutions(reportId: Int) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        _uiState.value = state.copy(evolutionsLoading = true)
        viewModelScope.launch {
            getEvolutionsUseCase(reportId)
                .onSuccess { evolutions ->
                    (_uiState.value as? DetailUiState.Success)?.let {
                        _uiState.value = it.copy(evolutions = evolutions, evolutionsLoading = false)
                    }
                }
                .onFailure {
                    (_uiState.value as? DetailUiState.Success)?.let {
                        _uiState.value = it.copy(evolutionsLoading = false)
                    }
                }
        }
    }

    private fun loadComments(reportId: Int) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        _uiState.value = state.copy(commentsLoading = true)
        viewModelScope.launch {
            getCommentsUseCase(reportId)
                .onSuccess { comments ->
                    (_uiState.value as? DetailUiState.Success)?.let {
                        _uiState.value = it.copy(comments = comments, commentsLoading = false)
                    }
                }
                .onFailure {
                    (_uiState.value as? DetailUiState.Success)?.let {
                        _uiState.value = it.copy(commentsLoading = false)
                    }
                }
        }
    }

    fun toggleVote(isUpvote: Boolean) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        val report = state.report
        val newVoteValue = if (isUpvote) 1 else -1
        val finalVote = if (report.userVote == newVoteValue) 0 else newVoteValue
        val diff = finalVote - report.userVote

        _uiState.value = state.copy(
            report = report.copy(userVote = finalVote, credibilityScore = report.credibilityScore + diff)
        )
        viewModelScope.launch {
            voteReportUseCase(report.id, isUpvote).onFailure { _uiState.value = state }
        }
    }

    fun toggleEvolutionVote(evolutionId: Int, isUpvote: Boolean) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            voteEvolutionUseCase(evolutionId, isUpvote)
                .onSuccess { updated ->
                    (_uiState.value as? DetailUiState.Success)?.let { s ->
                        _uiState.value = s.copy(
                            evolutions = s.evolutions.map { if (it.id == updated.id) updated else it }
                        )
                    }
                }
        }
    }

    fun createEvolution(
        type: String,
        description: String,
        photoUrl: String?,
        userLat: Double,
        userLon: Double,
    ) {
        val reportId = currentReportId ?: return
        viewModelScope.launch {
            createEvolutionUseCase(reportId, type, description, photoUrl, userLat, userLon)
                .onSuccess { newEvo ->
                    (_uiState.value as? DetailUiState.Success)?.let { s ->
                        _uiState.value = s.copy(evolutions = s.evolutions + newEvo)
                    }
                }
                .onFailure { _evolutionError.value = it.message ?: "No se pudo publicar la actualización" }
        }
    }

    fun createComment(content: String) {
        val reportId = currentReportId ?: return
        viewModelScope.launch {
            createCommentUseCase(reportId, content)
                .onSuccess { newComment ->
                    (_uiState.value as? DetailUiState.Success)?.let { s ->
                        _uiState.value = s.copy(comments = s.comments + newComment)
                    }
                }
        }
    }

    fun deleteComment(commentId: Int) {
        val reportId = currentReportId ?: return
        val state = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            deleteCommentUseCase(reportId, commentId)
                .onSuccess {
                    (_uiState.value as? DetailUiState.Success)?.let { s ->
                        _uiState.value = s.copy(comments = s.comments.filter { it.id != commentId })
                    }
                }
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
                } else if (deletePhoto) {
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

    private fun handleRealtimeEvent(event: ReportRealtimeEvent) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        val reportId = currentReportId ?: return
        if (state.report.id != reportId) return

        when (event) {
            is ReportRealtimeEvent.NewReport -> {
                if (event.report.id != reportId) return
                _uiState.value = state.copy(report = event.report.copy(userVote = state.report.userVote))
            }
            is ReportRealtimeEvent.VoteUpdate -> {
                if (event.reportId != reportId) return
                _uiState.value = state.copy(
                    report = state.report.copy(credibilityScore = event.credibilityScore, status = event.status)
                )
            }
        }
    }
}
