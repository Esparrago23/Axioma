package com.patatus.axioma.features.reports.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.presentation.viewmodels.DetailUiState
import com.patatus.axioma.features.reports.presentation.viewmodels.ReportDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Int,
    viewModel: ReportDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(reportId) {
        viewModel.loadReport(reportId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Estado para controlar el menú de los 3 puntitos
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Deleted) {
            Toast.makeText(context, "Reporte eliminado", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // ⭐ LÓGICA DE LOS 3 PUNTITOS: Solo si cargo con éxito y soy el dueño
                    val state = uiState
                    if (state is DetailUiState.Success && state.report.authorId == state.currentUserId) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirmation = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Text("Error: ${state.msg}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadReport(reportId) }) {
                            Text("Reintentar")
                        }
                    }
                }
                is DetailUiState.Success -> {
                    // Ya no pasamos los callbacks de edit/delete aquí abajo
                    ReportDetailContent(
                        report = state.report,
                        currentUserId = state.currentUserId,
                        onVote = { isUpvote -> viewModel.toggleVote(isUpvote) }
                    )

                    if (showEditDialog) {
                        EditReportDialog(
                            report = state.report,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { title, desc, photoUri, deletePhoto ->
                                viewModel.updateReportWithMedia(state.report.id, title, desc, photoUri, deletePhoto)
                                showEditDialog = false
                            }
                        )
                    }

                    if (showDeleteConfirmation) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmation = false },
                            title = { Text("¿Eliminar reporte?") },
                            text = { Text("Esta acción no se puede deshacer. El reporte desaparecerá del mapa y del feed.") },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.deleteReport(state.report.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) { Text("Eliminar") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ReportDetailContent(
    report: Report,
    currentUserId: Int,
    onVote: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val fullImageUrl = remember(report.photoUrl) {
        val rawUrl = report.photoUrl ?: ""
        when {
            rawUrl.isBlank() -> ""
            rawUrl.startsWith("http") -> rawUrl
            else -> "${BuildConfig.BASE_URL_API.removeSuffix("/")}/${rawUrl.removePrefix("/")}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (fullImageUrl.isNotEmpty()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context).data(fullImageUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                loading = { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp)) } },
                error = { Icon(Icons.Default.BrokenImage, null, Modifier.size(40.dp).align(Alignment.Center), Color.Gray) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(report.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    AssistChip(onClick = {}, label = { Text(report.category) })
                    Text(report.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(report.description, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Reputación del reporte", style = MaterialTheme.typography.titleMedium)
                Text("${report.credibilityScore}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Botón REAL
                    val isUpvoted = report.userVote == 1
                    Button(
                        onClick = { onVote(true) },
                        modifier = Modifier.weight(1f),
                        colors = if (isUpvoted) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Icon(if (isUpvoted) Icons.Default.CheckCircle else Icons.Default.ThumbUp, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Real")
                    }

                    // Botón FALSO
                    val isDownvoted = report.userVote == -1
                    Button(
                        onClick = { onVote(false) },
                        modifier = Modifier.weight(1f),
                        colors = if (isDownvoted) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Icon(if (isDownvoted) Icons.Default.Cancel else Icons.Default.ThumbDown, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Falso")
                    }
                }
            }
        }

        // ⭐ La sección "Gestión de mi reporte" fue eliminada de aquí
    }
}

@Composable
fun EditReportDialog(
    report: Report,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(report.title) }
    var description by remember { mutableStateOf(report.description) }
    var localPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var shouldDeleteCurrentPhoto by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            localPhotoUri = uri
            shouldDeleteCurrentPhoto = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Reporte") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                Spacer(modifier = Modifier.height(20.dp))

                Text("Evidencia fotográfica", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (!shouldDeleteCurrentPhoto && (localPhotoUri != null || report.photoUrl != null)) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        AsyncImage(
                            model = localPhotoUri ?: report.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { shouldDeleteCurrentPhoto = true; localPhotoUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                    }
                } else {
                    OutlinedButton(onClick = { pickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AddAPhoto, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Subir nueva foto")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, description, localPhotoUri?.toString(), shouldDeleteCurrentPhoto) }) { Text("Guardar cambios") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}