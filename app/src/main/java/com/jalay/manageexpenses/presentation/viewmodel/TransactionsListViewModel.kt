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
    private val searchTransactionsUseCase: SearchTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionsListUiState>(TransactionsListUiState.Initial)
    val uiState: StateFlow<TransactionsListUiState> = _uiState.asStateFlow()

    private var searchQuery: String = ""

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = TransactionsListUiState.Loading
            try {
                getTransactionsUseCase().collect { transactions ->
                    _uiState.value = TransactionsListUiState.Success(
                        transactions = transactions,
                        filteredTransactions = transactions,
                        filterType = FilterType.ALL
                    )
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
                    _uiState.value = TransactionsListUiState.Success(
                        transactions = transactions,
                        filteredTransactions = transactions,
                        filterType = FilterType.ALL
                    )
                }
            }
        }
    }

    fun filterByType(type: FilterType) {
        viewModelScope.launch {
            val transactions = when (type) {
                FilterType.ALL -> getTransactionsUseCase()
                FilterType.SENT -> getTransactionsUseCase.byType(TransactionType.SENT)
                FilterType.RECEIVED -> getTransactionsUseCase.byType(TransactionType.RECEIVED)
            }
            transactions.collect { trans ->
                _uiState.value = TransactionsListUiState.Success(
                    transactions = trans,
                    filteredTransactions = trans,
                    filterType = type
                )
            }
        }
    }
}

sealed class TransactionsListUiState {
    object Initial : TransactionsListUiState()
    object Loading : TransactionsListUiState()
    data class Success(
        val transactions: List<Transaction>,
        val filteredTransactions: List<Transaction>,
        val filterType: FilterType
    ) : TransactionsListUiState()
    data class Error(val message: String) : TransactionsListUiState()
}

enum class FilterType {
    ALL, SENT, RECEIVED
}