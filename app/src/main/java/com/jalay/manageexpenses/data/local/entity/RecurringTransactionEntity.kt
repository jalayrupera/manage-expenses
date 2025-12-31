package com.jalay.manageexpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
