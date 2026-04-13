package com.patatus.axioma.features.reports.domain.entities

enum class FeedSort {
    NEARBY,
    RECENT,
    RELEVANT

}

data class FeedQuery(
    val sort: FeedSort = FeedSort.NEARBY,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusKm: Int = 15,
)
