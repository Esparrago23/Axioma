package com.patatus.axioma.features.users.presentation.screens

import com.patatus.axioma.features.users.domain.entities.User

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isDeleting: Boolean = false,
    val loggedOut: Boolean = false,
    val deletedAccount: Boolean = false,
    val user: User? = null,
    val usernameInput: String = "",
    val fullNameInput: String = "",
    val profilePictureUrlInput: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)
