package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.jalay.manageexpenses.domain.model.SortType
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import com.jalay.manageexpenses.domain.usecase.GetTransactionsUseCase
import com.jalay.manageexpenses.domain.usecase.SearchTransactionsUseCase
import com.jalay.manageexpenses.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TransactionsListViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val searchTransactionsUseCase: SearchTransactionsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val filterCategory: String? = savedStateHandle.get<String>("category")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType = _filterType.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    val sortType = _sortType.asStateFlow()

    val transactionsPaged: Flow<PagingData<TransactionListItem>> = combine(
        _searchQuery,
        _filterType,
        _sortType
    ) { query, filter, sort ->
        Triple(query, filter, sort)
    }.flatMapLatest { (query, filter, sort) ->
        val type = when (filter) {
            FilterType.ALL -> null
            FilterType.SENT -> TransactionType.SENT
            FilterType.RECEIVED -> TransactionType.RECEIVED
        }

        val flow = if (query.isBlank()) {
            if (filterCategory != null) {
                getTransactionsUseCase.pagedByCategory(filterCategory, type, sort)
            } else {
                getTransactionsUseCase.paged(type, sort)
            }
        } else {
            searchTransactionsUseCase.paged(query, type, sort)
        }

        // Only insert date headers for date-based sorting
        val shouldInsertDateHeaders = sort == SortType.DATE_DESC || sort == SortType.DATE_ASC

        flow.map { pagingData ->
            pagingData.map<Transaction, TransactionListItem> { TransactionListItem.TransactionItem(it) }
                .let { mappedData ->
                    if (shouldInsertDateHeaders) {
                        mappedData.insertSeparators { before, after ->
                            if (after == null) return@insertSeparators null

                            val afterItem = after as? TransactionListItem.TransactionItem ?: return@insertSeparators null
                            val afterDate = FormatUtils.formatDateHeader(afterItem.transaction.timestamp)

                            if (before == null) {
                                return@insertSeparators TransactionListItem.HeaderItem(afterDate)
                            }

                            val beforeItem = before as? TransactionListItem.TransactionItem ?: return@insertSeparators null
                            val beforeDate = FormatUtils.formatDateHeader(beforeItem.transaction.timestamp)

                            if (beforeDate != afterDate) {
                                TransactionListItem.HeaderItem(afterDate)
                            } else {
                                null
                            }
                        }
                    } else {
                        mappedData
                    }
                }
        }
    }.cachedIn(viewModelScope)

    fun searchTransactions(query: String) {
        _searchQuery.value = query
    }

    fun filterByType(type: FilterType) {
        _filterType.value = type
    }

    fun sortBy(sortType: SortType) {
        _sortType.value = sortType
    }
}

sealed class TransactionListItem {
    data class TransactionItem(val transaction: Transaction) : TransactionListItem()
    data class HeaderItem(val date: String) : TransactionListItem()
}

enum class FilterType {
    ALL, SENT, RECEIVED
}
