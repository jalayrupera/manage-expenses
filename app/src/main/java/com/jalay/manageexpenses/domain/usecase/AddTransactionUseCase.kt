package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Double,
        recipientName: String,
        transactionType: TransactionType,
        category: String,
        notes: String?,
        timestamp: Long = System.currentTimeMillis()
    ): Long? {
        val transaction = Transaction(
            id = null,
            amount = amount,
            recipientName = recipientName,
            transactionType = transactionType,
            category = category,
            notes = notes,
            timestamp = timestamp,
            rawSms = "",
            upiApp = "Manual",
            transactionRef = "MANUAL_${System.currentTimeMillis()}",
            isParsed = false
        )
        return repository.insertTransaction(transaction)
    }
}
