package com.patatus.axioma.features.reports.domain.entities

data class Report(
    val id: Int,
    val title: String,
    val description: String,
    val category: String, // "INFRASTRUCTURE", "SECURITY", etc.
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?, // Puede ser nulo
    val credibilityScore: Int,
    val status: String,    // "ACTIVE", "HIDDEN", "SOLVED"
    val createdAt: String  // Fecha en formato String ISO
)