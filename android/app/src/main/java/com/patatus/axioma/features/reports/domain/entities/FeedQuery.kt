package com.patatus.axioma.features.reports.domain.entities

enum class FeedSort {
    RELEVANT,
    RECENT
}

data class FeedQuery(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusKm: Int = 15,
    val sort: FeedSort = FeedSort.RECENT
)
