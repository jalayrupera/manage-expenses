package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.usecase.GetTransactionsUseCase
import com.jalay.manageexpenses.domain.usecase.UpdateCategoryUseCase
import com.jalay.manageexpenses.domain.usecase.UpdateTransactionNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val updateTransactionNotesUseCase: UpdateTransactionNotesUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<String>("transactionId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow<TransactionDetailUiState>(TransactionDetailUiState.Loading)
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val availableCategories = listOf(
        "Shopping", "Food & Dining", "Transport", "Utilities",
        "Entertainment", "Bills & Recharges", "Transfers", "UPI", "Other"
    )

    init {
        if (transactionId != 0L) {
            loadTransaction(transactionId)
        }
    }

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            _uiState.value = TransactionDetailUiState.Loading
            try {
                val transactions = getTransactionsUseCase().first()
                val transaction = transactions.firstOrNull { it.id == transactionId }
                    ?: throw Exception("Transaction not found")

                _uiState.value = TransactionDetailUiState.Success(
                    transaction = transaction,
                    availableCategories = availableCategories
                )
            } catch (e: Exception) {
                _uiState.value = TransactionDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateNotes(notes: String?) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is TransactionDetailUiState.Success && state.transaction != null) {
                updateTransactionNotesUseCase(state.transaction.id!!, notes)
                loadTransaction(state.transaction.id!!)
            }
        }
    }

    fun updateCategory(category: String) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is TransactionDetailUiState.Success && state.transaction != null) {
                updateCategoryUseCase(state.transaction.id!!, category)
                loadTransaction(state.transaction.id!!)
            }
        }
    }
}

sealed class TransactionDetailUiState {
    object Loading : TransactionDetailUiState()
    data class Success(
        val transaction: Transaction?,
        val availableCategories: List<String>
    ) : TransactionDetailUiState()
    data class Error(val message: String) : TransactionDetailUiState()
}
