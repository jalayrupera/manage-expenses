package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.domain.model.TransactionType
import com.jalay.manageexpenses.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Input())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    val availableCategories = listOf(
        "Shopping", "Food & Dining", "Transport", "Utilities",
        "Entertainment", "Bills & Recharges", "Transfers", "Groceries",
        "Healthcare", "Education", "Travel", "Other"
    )

    fun updateAmount(amount: String) {
        val currentState = _uiState.value
        if (currentState is AddTransactionUiState.Input) {
            _uiState.value = currentState.copy(amount = amount)
        }
    }

    fun updateRecipient(recipient: String) {
        val currentState = _uiState.value
        if (currentState is AddTransactionUiState.Input) {
            _uiState.value = currentState.copy(recipientName = recipient)
        }
    }

    fun updateTransactionType(type: TransactionType) {
        val currentState = _uiState.value
        if (currentState is AddTransactionUiState.Input) {
            _uiState.value = currentState.copy(transactionType = type)
        }
    }

    fun updateCategory(category: String) {
        val currentState = _uiState.value
        if (currentState is AddTransactionUiState.Input) {
            _uiState.value = currentState.copy(category = category)
        }
    }

    fun updateNotes(notes: String) {
        val currentState = _uiState.value
        if (currentState is AddTransactionUiState.Input) {
            _uiState.value = currentState.copy(notes = notes)
        }
    }

    fun updateDate(timestamp: Long) {
        val currentState = _uiState.value
        if (currentState is AddTransactionUiState.Input) {
            _uiState.value = currentState.copy(timestamp = timestamp)
        }
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        if (currentState !is AddTransactionUiState.Input) return

        // Validate input
        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = currentState.copy(error = "Please enter a valid amount")
            return
        }

        if (currentState.recipientName.isBlank()) {
            _uiState.value = currentState.copy(error = "Please enter a recipient name")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddTransactionUiState.Saving

            try {
                val result = addTransactionUseCase(
                    amount = amount,
                    recipientName = currentState.recipientName.trim(),
                    transactionType = currentState.transactionType,
                    category = currentState.category,
                    notes = currentState.notes.ifBlank { null },
                    timestamp = currentState.timestamp
                )

                if (result != null) {
                    _uiState.value = AddTransactionUiState.Success
                } else {
                    _uiState.value = AddTransactionUiState.Input(error = "Failed to save transaction")
                }
            } catch (e: Exception) {
                _uiState.value = AddTransactionUiState.Input(error = e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = AddTransactionUiState.Input()
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is AddTransactionUiState.Input) {
            _uiState.value = currentState.copy(error = null)
        }
    }
}

sealed class AddTransactionUiState {
    data class Input(
        val amount: String = "",
        val recipientName: String = "",
        val transactionType: TransactionType = TransactionType.SENT,
        val category: String = "Other",
        val notes: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val error: String? = null
    ) : AddTransactionUiState()

    object Saving : AddTransactionUiState()
    object Success : AddTransactionUiState()
}
