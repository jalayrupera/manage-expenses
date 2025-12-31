package com.jalay.manageexpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jalay.manageexpenses.domain.model.RecurringFrequency
import com.jalay.manageexpenses.domain.model.RecurringTransaction

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val nextDate: Long,
    val notes: String? = null,
    val isActive: Boolean = true
) {
    fun toDomainModel(): RecurringTransaction = RecurringTransaction(
        id = id,
        name = name,
        amount = amount,
        category = category,
        frequency = RecurringFrequency.valueOf(frequency),
        nextDate = nextDate,
        notes = notes,
        isActive = isActive
    )
}
