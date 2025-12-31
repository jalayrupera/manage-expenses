package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.data.parser.CategoryAutoMapper
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.CategoryMapping
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryRulesViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryAutoMapper: CategoryAutoMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryRulesUiState>(CategoryRulesUiState.Loading)
    val uiState: StateFlow<CategoryRulesUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        viewModelScope.launch {
            _uiState.value = CategoryRulesUiState.Loading
            try {
                transactionRepository.getAllCategoryMappings().collect { mappings ->
                    val groupedByCategory = mappings.groupBy { it.category }
                    _uiState.value = CategoryRulesUiState.Success(
                        rules = mappings,
                        groupedRules = groupedByCategory,
                        availableCategories = getAvailableCategories()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CategoryRulesUiState.Error(e.message ?: "Failed to load rules")
            }
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addRule(keyword: String, category: String) {
        viewModelScope.launch {
            try {
                val icon = getCategoryIcon(category)
                val mapping = CategoryMapping(
                    keyword = keyword.lowercase().trim(),
                    category = category,
                    icon = icon,
                    isCustom = true
                )
                transactionRepository.insertCategoryMapping(mapping)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateRule(rule: CategoryMapping, newKeyword: String, newCategory: String) {
        viewModelScope.launch {
            try {
                // Delete old rule and insert updated one
                transactionRepository.deleteCategoryMapping(rule)
                val icon = getCategoryIcon(newCategory)
                val updatedMapping = CategoryMapping(
                    keyword = newKeyword.lowercase().trim(),
                    category = newCategory,
                    icon = icon,
                    isCustom = true
                )
                transactionRepository.insertCategoryMapping(updatedMapping)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteRule(rule: CategoryMapping) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteCategoryMapping(rule)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                // Delete all custom mappings and re-insert defaults
                val currentMappings = transactionRepository.getAllCategoryMappingsSync()
                currentMappings.forEach { transactionRepository.deleteCategoryMapping(it) }
                
                val defaults = categoryAutoMapper.getDefaultMappings()
                transactionRepository.insertCategoryMappings(defaults)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun getAvailableCategories(): List<String> {
        return listOf(
            "Shopping",
            "Food & Dining",
            "Transport",
            "Utilities",
            "Entertainment",
            "Bills & Recharges",
            "Transfers",
            "Healthcare",
            "Education",
            "Travel",
            "Personal Care",
            "Groceries",
            "Other"
        )
    }

    private fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "shopping" -> "shopping_bag"
            "food & dining" -> "restaurant"
            "transport" -> "directions_car"
            "utilities" -> "bolt"
            "entertainment" -> "movie"
            "bills & recharges" -> "phone_android"
            "transfers" -> "account_balance"
            "healthcare" -> "local_hospital"
            "education" -> "school"
            "travel" -> "flight"
            "personal care" -> "spa"
            "groceries" -> "local_grocery_store"
            else -> "category"
        }
    }
}

sealed class CategoryRulesUiState {
    object Loading : CategoryRulesUiState()
    data class Success(
        val rules: List<CategoryMapping>,
        val groupedRules: Map<String, List<CategoryMapping>>,
        val availableCategories: List<String>
    ) : CategoryRulesUiState()
    data class Error(val message: String) : CategoryRulesUiState()
}
