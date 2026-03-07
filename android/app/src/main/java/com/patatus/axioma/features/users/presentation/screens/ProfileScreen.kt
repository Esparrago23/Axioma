package com.patatus.axioma.features.users.presentation.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.patatus.axioma.features.users.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackState = remember { SnackbarHostState() }
    var editMode by rememberSaveable { mutableStateOf(ProfileEditMode.NONE.name) }
    val isDataEditing = editMode == ProfileEditMode.DATA.name
    val isPhotoEditing = editMode == ProfileEditMode.PHOTO.name
    val brandColor = Color(0xFF64B6AB)
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onProfilePictureUrlChanged(uri.toString())
        }
    }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        val message = state.errorMessage ?: state.successMessage
        if (!message.isNullOrBlank()) {
            snackState.showSnackbar(message)
            viewModel.consumeMessages()
        }
    }

    LaunchedEffect(state.deletedAccount) {
        if (state.deletedAccount) {
            onAccountDeleted()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackState) }) { padding ->
        if (state.isLoading && state.user == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Text(
                    text = "Axioma",
                    color = brandColor,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-8).dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            AsyncImage(
                model = state.profilePictureUrlInput.ifBlank { null },
                contentDescription = "Avatar de usuario",
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = Color(0xFF202020), shape = CircleShape),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "Editar",
                color = brandColor,
                fontWeight = FontWeight.Medium,
                fontSize = 34.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { editMode = ProfileEditMode.PHOTO.name }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileInfoRow(
                icon = Icons.Outlined.AlternateEmail,
                title = "Nombre de Usuario",
                value = state.user?.username.orEmpty()
            )
            ProfileInfoRow(
                icon = Icons.Outlined.PersonOutline,
                title = "Nombre",
                value = state.user?.fullName ?: "Sin nombre"
            )
            ProfileInfoRow(
                icon = Icons.Outlined.StarOutline,
                title = "Reputacion",
                value = "${state.user?.reputation ?: 0} puntos de 10"
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Email,
                title = "Correo",
                value = state.user?.email.orEmpty(),
                underlineValue = true
            )
            ProfileInfoRow(
                icon = Icons.Outlined.CalendarMonth,
                title = "Miembro desde",
                value = formatMemberSince(state.user?.createdAt)
            )
            ProfileInfoRow(
                icon = Icons.Outlined.ErrorOutline,
                title = "Editar Datos",
                value = "",
                clickable = true,
                onClick = { editMode = ProfileEditMode.DATA.name }
            )

            if (isDataEditing) {
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = state.usernameInput,
                    onValueChange = viewModel::onUsernameChanged,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.fullNameInput,
                    onValueChange = viewModel::onFullNameChanged,
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveProfileChanges()
                            editMode = ProfileEditMode.NONE.name
                        },
                        enabled = !state.isSaving && !state.isDeleting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (state.isSaving) "Guardando..." else "Guardar")
                    }

                    TextButton(
                        onClick = { editMode = ProfileEditMode.NONE.name },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            if (isPhotoEditing) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Cambiar foto de perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    enabled = !state.isSaving && !state.isDeleting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Subir archivo")
                }

                Text(
                    text = "Archivo seleccionado: ${state.profilePictureUrlInput.takeIf { it.isNotBlank() } ?: "Ninguno"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A4A4A)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveProfileChanges()
                            editMode = ProfileEditMode.NONE.name
                        },
                        enabled = !state.isSaving && !state.isDeleting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (state.isSaving) "Guardando..." else "Guardar foto")
                    }

                    TextButton(
                        onClick = { editMode = ProfileEditMode.NONE.name },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            TextButton(
                onClick = viewModel::deleteAccount,
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isDeleting) "Eliminando..." else "Eliminar cuenta")
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    underlineValue: Boolean = false,
    clickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickable) {
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onClick?.invoke() }
                        .padding(vertical = 4.dp)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4A4A4A),
                    textDecoration = if (underlineValue) TextDecoration.Underline else TextDecoration.None
                )
            }
        }
    }
}

private fun formatMemberSince(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return "-"
    return createdAt.substringBefore("T")
}

private enum class ProfileEditMode {
    NONE,
    DATA,
    PHOTO
}
