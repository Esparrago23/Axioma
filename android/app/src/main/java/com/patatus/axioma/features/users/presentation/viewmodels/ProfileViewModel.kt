package com.patatus.axioma.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patatus.axioma.core.network.TokenManager
import com.patatus.axioma.features.users.domain.usecases.DeleteUserAccountUseCase
import com.patatus.axioma.features.users.domain.usecases.GetUserProfileUseCase
import com.patatus.axioma.features.users.domain.usecases.UpdateUserProfileUseCase
import com.patatus.axioma.features.users.presentation.screens.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val deleteUserAccountUseCase: DeleteUserAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, deletedAccount = false) }
            getUserProfileUseCase()
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = user,
                        usernameInput = user.username,
                        fullNameInput = user.fullName.orEmpty(),
                        profilePictureUrlInput = user.profilePicture.orEmpty(),
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo cargar el perfil"
                        )
                    }
                }
        }
    }

    fun onUsernameChanged(value: String) {
        _state.update { it.copy(usernameInput = value) }
    }

    fun onFullNameChanged(value: String) {
        _state.update { it.copy(fullNameInput = value) }
    }

    fun onProfilePictureUrlChanged(value: String) {
        _state.update { it.copy(profilePictureUrlInput = value) }
    }

    fun saveProfileChanges() {
        val current = _state.value
        if (current.usernameInput.isBlank()) {
            _state.update { it.copy(errorMessage = "El username no puede estar vacio") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

            updateUserProfileUseCase(
                username = current.usernameInput.trim(),
                fullName = current.fullNameInput.trim().ifBlank { null },
                profilePictureUrl = current.profilePictureUrlInput.trim().ifBlank { null }
            ).onSuccess { updatedUser ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    user = updatedUser,
                    usernameInput = updatedUser.username,
                    fullNameInput = updatedUser.fullName.orEmpty(),
                    profilePictureUrlInput = updatedUser.profilePicture.orEmpty(),
                    successMessage = "Perfil actualizado",
                    errorMessage = null
                )
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "No se pudo actualizar el perfil"
                    )
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, errorMessage = null) }
            deleteUserAccountUseCase()
                .onSuccess {
                    TokenManager.clearToken()
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            deletedAccount = true,
                            successMessage = "Cuenta eliminada"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = error.message ?: "No se pudo eliminar la cuenta"
                        )
                    }
                }
        }
    }

    fun consumeMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
