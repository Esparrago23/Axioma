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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.patatus.axioma.features.comments.domain.entities.Comment
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.entities.ReportEvolution
import com.patatus.axioma.features.reports.presentation.viewmodels.DetailUiState
import com.patatus.axioma.features.reports.presentation.viewmodels.ReportDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Int,
    viewModel: ReportDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(reportId) { viewModel.loadReport(reportId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val evolutionError by viewModel.evolutionError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAddEvolutionDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Deleted) {
            Toast.makeText(context, "Reporte eliminado", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(evolutionError) {
        val msg = evolutionError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearEvolutionError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del reporte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val state = uiState
                    if (state is DetailUiState.Success && state.report.authorId == state.currentUserId) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = { menuExpanded = false; showEditDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { menuExpanded = false; showDeleteConfirmation = true }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is DetailUiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Text("Error: ${state.msg}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadReport(reportId) }) { Text("Reintentar") }
                }
                is DetailUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                        ReportDetailContent(
                            report = state.report,
                            currentUserId = state.currentUserId,
                            onVote = { isUpvote -> viewModel.toggleVote(isUpvote) }
                        )

                        Spacer(Modifier.height(24.dp))

                        EvolutionSection(
                            evolutions = state.evolutions,
                            loading = state.evolutionsLoading,
                            currentUserId = state.currentUserId,
                            onVoteEvolution = { id, up -> viewModel.toggleEvolutionVote(id, up) },
                            onAddEvolution = { showAddEvolutionDialog = true }
                        )

                        Spacer(Modifier.height(24.dp))

                        CommentsSection(
                            comments = state.comments,
                            loading = state.commentsLoading,
                            currentUserId = state.currentUserId,
                            onSendComment = { content -> viewModel.createComment(content) },
                            onDeleteComment = { commentId -> viewModel.deleteComment(commentId) }
                        )
                    }

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
                            text = { Text("Esta acción no se puede deshacer.") },
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

                    if (showAddEvolutionDialog) {
                        AddEvolutionDialog(
                            onDismiss = { showAddEvolutionDialog = false },
                            onConfirm = { type, description, photoUrl, lat, lon ->
                                viewModel.createEvolution(type, description, photoUrl, lat, lon)
                                showAddEvolutionDialog = false
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
fun ReportDetailContent(report: Report, currentUserId: Int, onVote: (Boolean) -> Unit) {
    val context = LocalContext.current
    val fullImageUrl = remember(report.photoUrl) {
        val raw = report.photoUrl ?: ""
        when {
            raw.isBlank() -> ""
            raw.startsWith("http") -> raw
            else -> "${BuildConfig.BASE_URL_API.removeSuffix("/")}/${raw.removePrefix("/")}"
        }
    }

    if (fullImageUrl.isNotEmpty()) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context).data(fullImageUrl).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp)) } },
            error = { Icon(Icons.Default.BrokenImage, null, Modifier.size(40.dp).align(Alignment.Center), Color.Gray) }
        )
        Spacer(Modifier.height(16.dp))
    }

    Card(shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(report.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                AssistChip(onClick = {}, label = { Text(report.category) })
                Text(report.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            Text(report.description, style = MaterialTheme.typography.bodyLarge)
        }
    }

    Spacer(Modifier.height(16.dp))

    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Reputación del reporte", style = MaterialTheme.typography.titleMedium)
            Text("${report.credibilityScore}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
}

@Composable
fun EvolutionSection(
    evolutions: List<ReportEvolution>,
    loading: Boolean,
    currentUserId: Int,
    onVoteEvolution: (Int, Boolean) -> Unit,
    onAddEvolution: () -> Unit,
) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Evolución del reporte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilledTonalButton(onClick = onAddEvolution, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Actualizar", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
            } else if (evolutions.isEmpty()) {
                Text("Sin actualizaciones aún. Sé el primero en reportar un cambio.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                evolutions.forEachIndexed { index, evo ->
                    EvolutionItem(
                        evolution = evo,
                        isLast = index == evolutions.lastIndex,
                        currentUserId = currentUserId,
                        onVote = { isUpvote -> onVoteEvolution(evo.id, isUpvote) }
                    )
                }
            }
        }
    }
}

@Composable
fun EvolutionItem(evolution: ReportEvolution, isLast: Boolean, currentUserId: Int, onVote: (Boolean) -> Unit) {
    val (icon, color, label) = when (evolution.type) {
        "WORSENED"  -> Triple(Icons.Default.TrendingDown, Color(0xFFD32F2F), "Empeoró")
        "IMPROVING" -> Triple(Icons.Default.TrendingUp,   Color(0xFF388E3C), "Mejorando")
        "RESOLVED"  -> Triple(Icons.Default.CheckCircle,  Color(0xFF1976D2), "Resuelto")
        "ACTIVE"    -> Triple(Icons.Default.RadioButtonChecked, Color(0xFFF57C00), "Sigue activo")
        "ESCALATED" -> Triple(Icons.Default.Warning,      Color(0xFFD32F2F), "Escaló")
        else        -> Triple(Icons.Default.Info,          Color.Gray, evolution.type)
    }

    val statusColor = when (evolution.status) {
        "CONFIRMED" -> MaterialTheme.colorScheme.tertiary
        "REJECTED"  -> MaterialTheme.colorScheme.error
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
                if (evolution.status != "PENDING") {
                    Text(
                        if (evolution.status == "CONFIRMED") "✓ Confirmado" else "✗ Rechazado",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            Text(evolution.description, style = MaterialTheme.typography.bodySmall)

            if (evolution.photoUrl != null) {
                Spacer(Modifier.height(4.dp))
                AsyncImage(
                    model = evolution.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${evolution.credibilityScore}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (evolution.userId != currentUserId) {
                    TextButton(
                        onClick = { onVote(true) },
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        Icon(
                            if (evolution.userVote == 1) Icons.Default.ThumbUp else Icons.Default.ThumbUp,
                            null,
                            Modifier.size(14.dp),
                            tint = if (evolution.userVote == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = { onVote(false) },
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        Icon(
                            Icons.Default.ThumbDown,
                            null,
                            Modifier.size(14.dp),
                            tint = if (evolution.userVote == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun CommentsSection(
    comments: List<Comment>,
    loading: Boolean,
    currentUserId: Int,
    onSendComment: (String) -> Unit,
    onDeleteComment: (Int) -> Unit,
) {
    var commentText by remember { mutableStateOf("") }

    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Comentarios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
            } else if (comments.isEmpty()) {
                Text("Sin comentarios aún.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        isOwn = comment.userId == currentUserId,
                        onDelete = { onDeleteComment(comment.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { if (it.length <= 500) commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un comentario...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                )
                IconButton(
                    onClick = {
                        val text = commentText.trim()
                        if (text.isNotEmpty()) {
                            onSendComment(text)
                            commentText = ""
                        }
                    },
                    enabled = commentText.trim().isNotEmpty()
                ) {
                    Icon(Icons.Default.Send, "Enviar", tint = if (commentText.trim().isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, isOwn: Boolean, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (isOwn) {
            IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar comentario?") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun AddEvolutionDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, description: String, photoUrl: String?, lat: Double, lon: Double) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf("ACTIVE") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var userLat by remember { mutableStateOf(0.0) }
    var userLon by remember { mutableStateOf(0.0) }
    var locationChecked by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            try {
                val location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                if (location != null) {
                    userLat = location.latitude
                    userLon = location.longitude
                    locationChecked = true
                } else {
                    Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    val evolutionTypes = listOf(
        "ACTIVE"    to "Sigue activo",
        "WORSENED"  to "Empeoró",
        "IMPROVING" to "Mejorando",
        "ESCALATED" to "Escaló",
        "RESOLVED"  to "Resuelto",
    )

    LaunchedEffect(Unit) {
        locationLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar actualización") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (!locationChecked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Verificando ubicación...", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Text("¿Qué cambió?", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))

                evolutionTypes.forEach { (type, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Text(label, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 300) description = it },
                    label = { Text("Descripción del cambio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )

                Spacer(Modifier.height(12.dp))

                if (photoUri != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photoUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                    }
                } else {
                    OutlinedButton(
                        onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddAPhoto, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar foto (opcional)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedType, description, photoUri?.toString(), userLat, userLon) },
                enabled = locationChecked && description.trim().isNotEmpty()
            ) { Text("Publicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
        if (uri != null) { localPhotoUri = uri; shouldDeleteCurrentPhoto = false }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Reporte") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Spacer(Modifier.height(20.dp))
                Text("Evidencia fotográfica", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))

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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
