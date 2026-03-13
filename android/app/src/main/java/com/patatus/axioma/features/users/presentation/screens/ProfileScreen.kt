package com.patatus.axioma.features.users.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.patatus.axioma.core.hardware.camera.rememberCameraCaptureLauncher
import com.patatus.axioma.core.hardware.camera.rememberCameraPermissionRequester
import com.patatus.axioma.features.users.presentation.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackState = remember { SnackbarHostState() }

    var editMode by rememberSaveable { mutableStateOf(ProfileEditMode.NONE.name) }
    val isPhotoEditing = editMode == ProfileEditMode.PHOTO.name

    // Estados para modales
    var showEditDataDialog by rememberSaveable { mutableStateOf(false) }
    var showPhotoSourceSheet by rememberSaveable { mutableStateOf(false) }
    var showCameraRationaleDialog by rememberSaveable { mutableStateOf(false) }
    var pendingCameraLaunch by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    val brandColor = Color(0xFF64B6AB)

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onProfilePictureUrlChanged(uri.toString())
        }
    }

    val cameraPermission = rememberCameraPermissionRequester()
    val cameraLauncher = rememberCameraCaptureLauncher { photoUri ->
        viewModel.onProfilePictureUrlChanged(photoUri.toString())
    }

    LaunchedEffect(cameraPermission.hasPermission, pendingCameraLaunch) {
        if (cameraPermission.hasPermission && pendingCameraLaunch) {
            pendingCameraLaunch = false
            cameraLauncher.launch()
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

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            onLoggedOut()
        }
    }

    // Modal de Foto de Perfil
    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Foto del perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider()

                PhotoSourceItem(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Cámara",
                    onClick = {
                        showPhotoSourceSheet = false
                        editMode = ProfileEditMode.PHOTO.name
                        if (cameraPermission.hasPermission) {
                            cameraLauncher.launch()
                        } else {
                            pendingCameraLaunch = true
                            if (cameraPermission.shouldShowRationale) {
                                showCameraRationaleDialog = true
                            } else {
                                cameraPermission.requestPermission()
                            }
                        }
                    }
                )

                PhotoSourceItem(
                    icon = Icons.Outlined.Image,
                    title = "Galería",
                    onClick = {
                        showPhotoSourceSheet = false
                        editMode = ProfileEditMode.PHOTO.name
                        pickImageLauncher.launch("image/*")
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Modal de permisos de cámara
    if (showCameraRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showCameraRationaleDialog = false },
            title = { Text("Permiso de cámara") },
            text = { Text("Necesitamos acceso a la cámara para tomar la foto de perfil.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCameraRationaleDialog = false
                        cameraPermission.requestPermission()
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingCameraLaunch = false
                        showCameraRationaleDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para Editar Datos (Evita empujar el contenido hacia abajo)
    if (showEditDataDialog) {
        Dialog(onDismissRequest = { showEditDataDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Editar Perfil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = brandColor
                    )

                    OutlinedTextField(
                        value = state.usernameInput,
                        onValueChange = viewModel::onUsernameChanged,
                        label = { Text("Nombre de Usuario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.fullNameInput,
                        onValueChange = viewModel::onFullNameChanged,
                        label = { Text("Nombre Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showEditDataDialog = false }
                        ) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.saveProfileDataChanges()
                                showEditDataDialog = false
                            },
                            enabled = !state.isSaving
                        ) {
                            Text(if (state.isSaving) "Guardando..." else "Guardar")
                        }
                    }
                }
            }
        }
    }

    // Modal de Confirmación para Eliminar Cuenta
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = {
                Text(
                    text = "¿Eliminar cuenta?",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Esta acción es irreversible. Se perderán todos tus datos, reportes y reputación en Axioma. ¿Estás completamente seguro de que deseas eliminar tu cuenta?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
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
            // Cabecera superior
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

            // Avatar
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
                    .clickable { showPhotoSourceSheet = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Información del perfil
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
                title = "Reputación",
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
                icon = Icons.Outlined.Edit,
                title = "Editar Datos",
                value = "",
                clickable = true,
                onClick = { showEditDataDialog = true } // Abre el modal de edición
            )

            // Confirmación de foto editada (Se mantiene aquí por si cambió su avatar)
            if (isPhotoEditing) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Foto lista para guardar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveProfilePhotoChanges()
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

            // Botón de eliminar cuenta
            TextButton(
                onClick = { viewModel.logout() },
                enabled = !state.isSaving && !state.isDeleting && !state.isLoggingOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.isLoggingOut) "Cerrando sesión..." else "Cerrar sesión",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextButton(
                onClick = { showDeleteConfirmationDialog = true },
                enabled = !state.isSaving && !state.isDeleting && !state.isLoggingOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.isDeleting) "Eliminando..." else "Eliminar cuenta",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ======================================================================
// FUNCIONES Y COMPONENTES PRIVADOS (NO BORRAR)
// ======================================================================

@Composable
private fun PhotoSourceItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
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