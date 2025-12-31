package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
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
        _filterType
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        val type = when (filter) {
            FilterType.ALL -> null
            FilterType.SENT -> TransactionType.SENT
            FilterType.RECEIVED -> TransactionType.RECEIVED
        }

        val flow = if (query.isBlank()) {
            if (filterCategory != null) {
                getTransactionsUseCase.pagedByCategory(filterCategory, type)
            } else {
                getTransactionsUseCase.paged(type)
            }
        } else {
            searchTransactionsUseCase.paged(query, type)
        }
        
        flow.map { pagingData ->
            pagingData.map { TransactionListItem.TransactionItem(it) }
                .insertSeparators { before, after ->
                    if (after == null) return@insertSeparators null
                    
                    val afterDate = FormatUtils.formatDateHeader(after.transaction.timestamp)
                    if (before == null) {
                        return@insertSeparators TransactionListItem.HeaderItem(afterDate)
                    }
                    
                    val beforeDate = FormatUtils.formatDateHeader(before.transaction.timestamp)
                    if (beforeDate != afterDate) {
                        TransactionListItem.HeaderItem(afterDate)
                    } else {
                        null
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

enum class SortType {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}
