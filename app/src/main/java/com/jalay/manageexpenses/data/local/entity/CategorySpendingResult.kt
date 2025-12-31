package com.jalay.manageexpenses.data.local.entity

/**
 * Data class for aggregated category spending query result.
 * Used to optimize budget spending calculations by avoiding N+1 queries.
 */
data class CategorySpendingResult(
    val category: String,
    val totalSpending: Double
)
