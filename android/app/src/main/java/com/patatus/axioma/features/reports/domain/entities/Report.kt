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
    val createdAt: String,  // Fecha en formato String ISO
    val authorId: Int,    // Este es el 'user_id' de tu API
    val userVote: Int = 0, // Esto lo calcularemos o lo recibiremos (1, 0, -1)
)