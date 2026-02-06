package com.patatus.axioma.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun StatusChip(status: String) {
    val (containerColor, contentColor) = when (status) {
        "ACTIVE" -> Color(0xFFE8DEF8) to Color(0xFF1D192B)
        "RESOLVED" -> Color(0xFFC4EED0) to Color(0xFF072111)
        "HIDDEN" -> Color(0xFFFFD8E4) to Color(0xFF31111D)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AssistChip(
        onClick = {},
        label = { Text(status, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = null,
        modifier = Modifier.padding(end = 4.dp)
    )
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "INFRAESTRUCTURA" -> Icons.Default.Build
        "SEGURIDAD" -> Icons.Default.Security
        "SANITIZACION" -> Icons.Default.CleaningServices
        "VANDALISMO" -> Icons.Default.ReportProblem
        "SOCIAL" -> Icons.Default.Groups
        else -> Icons.Default.Category
    }
}