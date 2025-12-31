package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.Transaction
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }

    fun paged(type: com.jalay.manageexpenses.domain.model.TransactionType? = null): Flow<PagingData<Transaction>> {
        return repository.getPagedTransactions(type = type)
    }

    fun byType(type: com.jalay.manageexpenses.domain.model.TransactionType): Flow<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun byCategory(category: String): Flow<List<Transaction>> {
        return repository.getTransactionsByCategory(category)
    }

    fun pagedByCategory(category: String, type: com.jalay.manageexpenses.domain.model.TransactionType? = null): Flow<PagingData<Transaction>> {
        return repository.getPagedTransactionsByCategory(category, type = type)
    }

    fun byDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return repository.getTransactionsByDateRange(startTime, endTime)
    }
}