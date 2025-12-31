package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository

class UpdateTransactionNotesUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transactionId: Long,
        notes: String?
    ) {
        val transaction = repository.getTransactionById(transactionId) ?: return
        val updatedTransaction = transaction.copy(notes = notes)
        repository.updateTransaction(updatedTransaction)
    }
}