package com.jalay.manageexpenses.domain.model

data class RecurringTransaction(
    val id: Long? = null,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: RecurringFrequency,
    val nextDate: Long,
    val notes: String? = null,
    val isActive: Boolean = true
)

enum class RecurringFrequency {
    DAILY, WEEKLY, MONTHLY, YEARLY
}
