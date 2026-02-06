package com.patatus.axioma.features.reports.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Int,
    viewModel: ReportDetailViewModel,
    onBack: () -> Unit
) {

    LaunchedEffect(reportId) {
        viewModel.loadReport(reportId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

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
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when (val state = uiState) {

                is DetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Error: ${state.msg}",
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(onClick = { viewModel.loadReport(reportId) }) {
                            Text("Reintentar")
                        }
                    }
                }

                is DetailUiState.Success -> {

                    ReportDetailContent(
                        report = state.report,
                        onVote = { viewModel.vote(state.report.id, it) },
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // CARD PRINCIPAL
        Card(
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    report.title,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(report.category) }
                    )

                    Text(
                        report.status,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    report.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ⭐ SECCIÓN VOTOS
        Card(
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    "Reputación",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${report.credibilityScore}",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    FilledTonalButton(
                        onClick = { onVote(true) }
                    ) {
                        Icon(Icons.Default.ThumbUp, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Real")
                    }

                    FilledTonalButton(
                        onClick = { onVote(false) }
                    ) {
                        Icon(Icons.Default.ThumbUp, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Falso")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ⭐ SECCIÓN ACCIONES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            OutlinedButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Editar")
            }

            OutlinedButton(
                onClick = onDeleteClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(modifier = Modifier.width(6.dp))
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

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    minLines = 3
                )
            }
        },

        confirmButton = {
            Button(onClick = { onConfirm(title, description) }) {
                Text("Guardar")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
