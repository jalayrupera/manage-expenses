package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }

    fun byType(type: com.jalay.manageexpenses.domain.model.TransactionType): Flow<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun byCategory(category: String): Flow<List<Transaction>> {
        return repository.getTransactionsByCategory(category)
    }

    fun byDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return repository.getTransactionsByDateRange(startTime, endTime)
    }
}