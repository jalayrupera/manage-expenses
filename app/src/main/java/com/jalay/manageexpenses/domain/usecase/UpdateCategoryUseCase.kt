package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository

class UpdateCategoryUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        transactionId: Long,
        category: String
    ) {
        val transaction = repository.getTransactionById(transactionId) ?: return
        val updatedTransaction = transaction.copy(category = category)
        repository.updateTransaction(updatedTransaction)
    }
}