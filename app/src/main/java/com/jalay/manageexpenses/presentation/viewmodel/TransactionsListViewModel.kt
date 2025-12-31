package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import com.jalay.manageexpenses.domain.usecase.GetTransactionsUseCase
import com.jalay.manageexpenses.domain.usecase.SearchTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsListViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val searchTransactionsUseCase: SearchTransactionsUseCase,
    private val filterCategory: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionsListUiState>(TransactionsListUiState.Initial)
    val uiState: StateFlow<TransactionsListUiState> = _uiState.asStateFlow()

    private var searchQuery: String = ""
    private var currentSortType: SortType = SortType.DATE_DESC
    private var currentFilterType: FilterType = FilterType.ALL
    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = TransactionsListUiState.Loading
            try {
                val transactionsFlow = if (filterCategory != null) {
                    getTransactionsUseCase.byCategory(filterCategory)
                } else {
                    getTransactionsUseCase()
                }

                transactionsFlow.collect { transactions ->
                    allTransactions = transactions
                    applyFiltersAndSort()
                }
            } catch (e: Exception) {
                _uiState.value = TransactionsListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchTransactions(query: String) {
        searchQuery = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadTransactions()
            } else {
                searchTransactionsUseCase(query).collect { transactions ->
                    val filtered = if (filterCategory != null) {
                        transactions.filter { it.category == filterCategory }
                    } else {
                        transactions
                    }
                    allTransactions = filtered
                    applyFiltersAndSort()
                }
            }
        }
    }

    fun filterByType(type: FilterType) {
        currentFilterType = type
        viewModelScope.launch {
            val baseFlow = if (filterCategory != null) {
                getTransactionsUseCase.byCategory(filterCategory)
            } else {
                when (type) {
                    FilterType.ALL -> getTransactionsUseCase()
                    FilterType.SENT -> getTransactionsUseCase.byType(TransactionType.SENT)
                    FilterType.RECEIVED -> getTransactionsUseCase.byType(TransactionType.RECEIVED)
                }
            }

            baseFlow.collect { transactions ->
                val filtered = when (type) {
                    FilterType.ALL -> transactions
                    FilterType.SENT -> transactions.filter { it.transactionType == TransactionType.SENT }
                    FilterType.RECEIVED -> transactions.filter { it.transactionType == TransactionType.RECEIVED }
                }
                allTransactions = filtered
                applyFiltersAndSort()
            }
        }
    }

    fun sortBy(sortType: SortType) {
        currentSortType = sortType
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val sorted = when (currentSortType) {
            SortType.DATE_DESC -> allTransactions.sortedByDescending { it.timestamp }
            SortType.DATE_ASC -> allTransactions.sortedBy { it.timestamp }
            SortType.AMOUNT_DESC -> allTransactions.sortedByDescending { it.amount }
            SortType.AMOUNT_ASC -> allTransactions.sortedBy { it.amount }
        }

        _uiState.value = TransactionsListUiState.Success(
            transactions = sorted,
            filteredTransactions = sorted,
            filterType = currentFilterType,
            sortType = currentSortType
        )
    }
}

sealed class TransactionsListUiState {
    object Initial : TransactionsListUiState()
    object Loading : TransactionsListUiState()
    data class Success(
        val transactions: List<Transaction>,
        val filteredTransactions: List<Transaction>,
        val filterType: FilterType,
        val sortType: SortType = SortType.DATE_DESC
    ) : TransactionsListUiState()
    data class Error(val message: String) : TransactionsListUiState()
}

enum class FilterType {
    ALL, SENT, RECEIVED
}

enum class SortType {
    DATE_DESC,   // Latest first (default)
    DATE_ASC,    // Oldest first
    AMOUNT_DESC, // Highest amount first
    AMOUNT_ASC   // Lowest amount first
}
