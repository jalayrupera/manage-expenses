package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.data.repository.BudgetRepository
import com.jalay.manageexpenses.domain.model.Budget
import com.jalay.manageexpenses.domain.model.BudgetPeriod
import com.jalay.manageexpenses.domain.model.BudgetWithSpending
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val _addEditState = MutableStateFlow<AddEditBudgetState>(AddEditBudgetState.Hidden)
    val addEditState: StateFlow<AddEditBudgetState> = _addEditState.asStateFlow()

    val availableCategories = listOf(
        "Shopping", "Food & Dining", "Transport", "Utilities",
        "Entertainment", "Bills & Recharges", "Transfers", "Groceries",
        "Healthcare", "Education", "Travel", "Other"
    )

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = BudgetUiState.Loading
            try {
                val budgetsWithSpending = budgetRepository.getBudgetsWithSpending()
                _uiState.value = BudgetUiState.Success(budgetsWithSpending)
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error(e.message ?: "Failed to load budgets")
            }
        }
    }

    fun showAddBudget() {
        _addEditState.value = AddEditBudgetState.Adding(
            category = availableCategories.first(),
            limitAmount = "",
            period = BudgetPeriod.MONTHLY,
            alertThreshold = 0.8f
        )
    }

    fun showEditBudget(budget: Budget) {
        _addEditState.value = AddEditBudgetState.Editing(
            budgetId = budget.id!!,
            category = budget.category,
            limitAmount = budget.limitAmount.toString(),
            period = budget.period,
            alertThreshold = budget.alertThreshold
        )
    }

    fun hideAddEdit() {
        _addEditState.value = AddEditBudgetState.Hidden
    }

    fun updateCategory(category: String) {
        val current = _addEditState.value
        when (current) {
            is AddEditBudgetState.Adding -> _addEditState.value = current.copy(category = category)
            is AddEditBudgetState.Editing -> _addEditState.value = current.copy(category = category)
            else -> {}
        }
    }

    fun updateLimitAmount(amount: String) {
        val current = _addEditState.value
        when (current) {
            is AddEditBudgetState.Adding -> _addEditState.value = current.copy(limitAmount = amount)
            is AddEditBudgetState.Editing -> _addEditState.value = current.copy(limitAmount = amount)
            else -> {}
        }
    }

    fun updatePeriod(period: BudgetPeriod) {
        val current = _addEditState.value
        when (current) {
            is AddEditBudgetState.Adding -> _addEditState.value = current.copy(period = period)
            is AddEditBudgetState.Editing -> _addEditState.value = current.copy(period = period)
            else -> {}
        }
    }

    fun updateAlertThreshold(threshold: Float) {
        val current = _addEditState.value
        when (current) {
            is AddEditBudgetState.Adding -> _addEditState.value = current.copy(alertThreshold = threshold)
            is AddEditBudgetState.Editing -> _addEditState.value = current.copy(alertThreshold = threshold)
            else -> {}
        }
    }

    fun saveBudget() {
        viewModelScope.launch {
            val current = _addEditState.value
            
            when (current) {
                is AddEditBudgetState.Adding -> {
                    val amount = current.limitAmount.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        _addEditState.value = current.copy(error = "Please enter a valid amount")
                        return@launch
                    }

                    try {
                        val budget = Budget(
                            category = current.category,
                            limitAmount = amount,
                            period = current.period,
                            alertThreshold = current.alertThreshold
                        )
                        budgetRepository.insertBudget(budget)
                        _addEditState.value = AddEditBudgetState.Hidden
                        loadBudgets()
                    } catch (e: Exception) {
                        _addEditState.value = current.copy(error = e.message ?: "Failed to save budget")
                    }
                }
                
                is AddEditBudgetState.Editing -> {
                    val amount = current.limitAmount.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        _addEditState.value = current.copy(error = "Please enter a valid amount")
                        return@launch
                    }

                    try {
                        val budget = Budget(
                            id = current.budgetId,
                            category = current.category,
                            limitAmount = amount,
                            period = current.period,
                            alertThreshold = current.alertThreshold
                        )
                        budgetRepository.updateBudget(budget)
                        _addEditState.value = AddEditBudgetState.Hidden
                        loadBudgets()
                    } catch (e: Exception) {
                        _addEditState.value = current.copy(error = e.message ?: "Failed to update budget")
                    }
                }
                
                else -> {}
            }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            try {
                budgetRepository.deactivateBudget(budgetId)
                loadBudgets()
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
}

sealed class BudgetUiState {
    object Loading : BudgetUiState()
    data class Success(val budgets: List<BudgetWithSpending>) : BudgetUiState()
    data class Error(val message: String) : BudgetUiState()
}

sealed class AddEditBudgetState {
    object Hidden : AddEditBudgetState()
    
    data class Adding(
        val category: String,
        val limitAmount: String,
        val period: BudgetPeriod,
        val alertThreshold: Float,
        val error: String? = null
    ) : AddEditBudgetState()
    
    data class Editing(
        val budgetId: Long,
        val category: String,
        val limitAmount: String,
        val period: BudgetPeriod,
        val alertThreshold: Float,
        val error: String? = null
    ) : AddEditBudgetState()
}
