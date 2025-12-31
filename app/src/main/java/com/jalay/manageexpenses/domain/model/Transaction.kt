package com.jalay.manageexpenses.domain.model

data class Transaction(
    val id: Long? = null,
    val amount: Double,
    val recipientName: String,
    val transactionType: TransactionType,
    val category: String,
    val notes: String?,
    val timestamp: Long,
    val rawSms: String,
    val upiApp: String,
    val transactionRef: String?,
    val isParsed: Boolean
)