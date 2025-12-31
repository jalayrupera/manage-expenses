package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class SearchTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(query: String): Flow<List<com.jalay.manageexpenses.domain.model.Transaction>> {
        return repository.searchTransactionsWithNotes(query)
    }
}