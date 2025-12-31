package com.jalay.manageexpenses.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val amount: Double,
    val recipientName: String,
    val transactionType: String,
    val category: String,
    val notes: String?,
    val timestamp: Long,
    val rawSms: String,
    val upiApp: String,
    val transactionRef: String?,
    val isParsed: Boolean
)