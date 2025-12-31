package com.jalay.manageexpenses.domain.model

data class CategorySummary(
    val category: String,
    val icon: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val sentAmount: Double,
    val receivedAmount: Double
)