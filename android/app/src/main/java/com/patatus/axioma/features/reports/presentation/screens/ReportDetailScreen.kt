package com.patatus.axioma.features.reports.presentation.screens


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.presentation.viewmodels.DetailUiState
import com.patatus.axioma.features.reports.presentation.viewmodels.ReportDetailViewModel

@Composable
fun ReportDetailScreen(
    reportId: Int,
    viewModel: ReportDetailViewModel,
    onBack: () -> Unit
) {
    // Cargar reporte al iniciar
    LaunchedEffect(reportId) {
        viewModel.loadReport(reportId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

    // Manejo de eliminación exitosa
    LaunchedEffect(uiState) {
        if (uiState is DetailUiState.Deleted) {
            Toast.makeText(context, "Reporte eliminado", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is DetailUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.msg}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadReport(reportId) }) { Text("Reintentar") }
                    }
                }
                is DetailUiState.Success -> {
                    ReportDetailContent(
                        report = state.report,
                        onVote = { isUp -> viewModel.vote(state.report.id, isUp) },
                        onEditClick = { showEditDialog = true },
                        onDeleteClick = { viewModel.deleteReport(state.report.id) }
                    )

                    if (showEditDialog) {
                        EditReportDialog(
                            report = state.report,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { title, desc ->
                                viewModel.updateReport(state.report.id, title, desc)
                                showEditDialog = false
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
    onVote: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = report.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            AssistChip(onClick = {}, label = { Text(report.category) })
            Text(text = report.status, style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = report.description, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Área de Votos
        Text("Reputación: ${report.credibilityScore}", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { onVote(true) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Real")
            }
            Button(onClick = { onVote(false) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Falso")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Área de Acciones (Editar/Borrar)
        // Nota: Idealmente validarías si el usuario es el dueño antes de mostrar esto
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text("Editar")
            }
            OutlinedButton(onClick = onDeleteClick, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text("Borrar")
            }
        }
    }
}

@Composable
fun EditReportDialog(
    report: Report,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(report.title) }
    var description by remember { mutableStateOf(report.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Reporte") },
        text = {
            Column {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, description) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}