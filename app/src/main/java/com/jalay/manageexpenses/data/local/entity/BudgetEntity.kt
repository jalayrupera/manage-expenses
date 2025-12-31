package com.jalay.manageexpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val category: String,
    val limitAmount: Double,
    val period: String, // "monthly" or "weekly"
    val alertThreshold: Float = 0.8f, // Alert when spending reaches this percentage
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
