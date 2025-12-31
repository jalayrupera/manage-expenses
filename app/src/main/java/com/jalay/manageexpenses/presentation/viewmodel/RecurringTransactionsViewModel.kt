package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.data.repository.RecurringTransactionRepository
import com.jalay.manageexpenses.domain.model.RecurringFrequency
import com.jalay.manageexpenses.domain.model.RecurringTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RecurringTransactionsViewModel @Inject constructor(
    private val repository: RecurringTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecurringTransactionsUiState>(RecurringTransactionsUiState.Loading)
    val uiState: StateFlow<RecurringTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadRecurringTransactions()
    }

    private fun loadRecurringTransactions() {
        viewModelScope.launch {
            repository.getAllRecurringTransactions().collect { transactions ->
                _uiState.value = RecurringTransactionsUiState.Success(transactions)
            }
        }
    }

    fun addRecurringTransaction(
        name: String,
        amount: Double,
        category: String,
        frequency: RecurringFrequency,
        startDate: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            val transaction = RecurringTransaction(
                name = name,
                amount = amount,
                category = category,
                frequency = frequency,
                nextDate = startDate,
                notes = notes
            )
            repository.addRecurringTransaction(transaction)
        }
    }

    fun deleteRecurringTransaction(transaction: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(transaction)
        }
    }

    fun toggleRecurringTransactionStatus(transaction: RecurringTransaction) {
        viewModelScope.launch {
            transaction.id?.let {
                repository.setRecurringTransactionStatus(it, !transaction.isActive)
            }
        }
    }
}

sealed class RecurringTransactionsUiState {
    object Loading : RecurringTransactionsUiState()
    data class Success(val transactions: List<RecurringTransaction>) : RecurringTransactionsUiState()
    data class Error(val message: String) : RecurringTransactionsUiState()
}
