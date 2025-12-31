package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.Transaction

class DeleteTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }

    /**
     * Restores a previously deleted transaction (for undo functionality).
     * Returns the new ID of the re-inserted transaction.
     */
    suspend fun restore(transaction: Transaction): Long? {
        return repository.insertTransaction(transaction)
    }
}
