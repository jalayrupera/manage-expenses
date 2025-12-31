package com.jalay.manageexpenses.domain.model

data class MonthlySummary(
    val month: String,
    val sentAmount: Double,
    val receivedAmount: Double,
    val transactionCount: Int
)