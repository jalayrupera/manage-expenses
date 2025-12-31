package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.usecase.DeleteTransactionUseCase
import com.jalay.manageexpenses.domain.usecase.GetTransactionsUseCase
import com.jalay.manageexpenses.domain.usecase.SaveCategoryRuleUseCase
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
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val saveCategoryRuleUseCase: SaveCategoryRuleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<String>("transactionId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow<TransactionDetailUiState>(TransactionDetailUiState.Loading)
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val _deleteEvent = MutableStateFlow<DeleteEvent?>(null)
    val deleteEvent: StateFlow<DeleteEvent?> = _deleteEvent.asStateFlow()

    private val _rulePromptEvent = MutableStateFlow<RulePromptEvent?>(null)
    val rulePromptEvent: StateFlow<RulePromptEvent?> = _rulePromptEvent.asStateFlow()

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
                val transaction = state.transaction
                updateCategoryUseCase(transaction.id!!, category)
                loadTransaction(transaction.id!!)

                // Check if we should prompt to save a rule
                val keyword = saveCategoryRuleUseCase.extractKeyword(transaction.recipientName)
                if (keyword.isNotEmpty() && !saveCategoryRuleUseCase.ruleExists(keyword)) {
                    _rulePromptEvent.value = RulePromptEvent(
                        keyword = keyword,
                        recipientName = transaction.recipientName,
                        category = category
                    )
                }
            }
        }
    }

    fun saveCategoryRule() {
        viewModelScope.launch {
            val event = _rulePromptEvent.value ?: return@launch
            saveCategoryRuleUseCase.save(event.keyword, event.category)
            _rulePromptEvent.value = null
        }
    }

    fun dismissRulePrompt() {
        _rulePromptEvent.value = null
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is TransactionDetailUiState.Success && state.transaction != null) {
                val transaction = state.transaction
                deleteTransactionUseCase(transaction)
                _deleteEvent.value = DeleteEvent.Deleted(transaction)
            }
        }
    }

    fun restoreTransaction() {
        viewModelScope.launch {
            val event = _deleteEvent.value
            if (event is DeleteEvent.Deleted) {
                deleteTransactionUseCase.restore(event.transaction)
                _deleteEvent.value = DeleteEvent.Restored
            }
        }
    }

    fun clearDeleteEvent() {
        _deleteEvent.value = null
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

sealed class DeleteEvent {
    data class Deleted(val transaction: Transaction) : DeleteEvent()
    object Restored : DeleteEvent()
}

data class RulePromptEvent(
    val keyword: String,
    val recipientName: String,
    val category: String
)
