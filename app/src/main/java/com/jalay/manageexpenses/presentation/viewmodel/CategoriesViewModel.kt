package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.domain.model.CategorySummary
import com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val statistics = getStatisticsUseCase()
                // Sort by sent amount (highest spending first)
                val sortedCategories = statistics.categorySummaries
                    .sortedByDescending { it.sentAmount }
                _uiState.value = CategoriesUiState.Success(sortedCategories)
            } catch (e: Exception) {
                _uiState.value = CategoriesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CategoriesUiState {
    object Loading : CategoriesUiState()
    data class Success(val categories: List<CategorySummary>) : CategoriesUiState()
    data class Error(val message: String) : CategoriesUiState()
}
