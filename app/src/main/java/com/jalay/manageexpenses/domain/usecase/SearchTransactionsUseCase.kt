package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.SortType
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class SearchTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(query: String): Flow<List<Transaction>> {
        return repository.searchTransactionsWithNotes(query)
    }

    fun paged(
        query: String,
        type: TransactionType? = null,
        sortType: SortType = SortType.DATE_DESC
    ): Flow<PagingData<Transaction>> {
        return repository.searchPagedTransactions(query, type = type, sortType = sortType)
    }
}