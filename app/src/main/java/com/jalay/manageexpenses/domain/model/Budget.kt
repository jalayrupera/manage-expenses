package com.jalay.manageexpenses.domain.model

data class Budget(
    val id: Long? = null,
    val category: String,
    val limitAmount: Double,
    val period: BudgetPeriod,
    val alertThreshold: Float = 0.8f,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BudgetPeriod {
    MONTHLY,
    WEEKLY
}

data class BudgetWithSpending(
    val budget: Budget,
    val currentSpending: Double,
    val remainingAmount: Double,
    val percentageUsed: Float,
    val isOverBudget: Boolean,
    val isNearLimit: Boolean
)
