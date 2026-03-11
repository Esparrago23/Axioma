package com.patatus.axioma.features.reports.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patatus.axioma.features.reports.presentation.viewmodels.MyReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    onReportClick: (Int) -> Unit, // Función para navegar al detalle
    viewModel: MyReportsViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar por título...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isBlank()) "Aún no has creado reportes." else "No hay resultados.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Lista de tus reportes
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    // Reemplaza esto con tu componente de tarjeta real (ReportCard o similar)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReportClick(report.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = report.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = report.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(text = report.status, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}