package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase
import com.jalay.manageexpenses.domain.usecase.ImportHistoricalSmsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val importHistoricalSmsUseCase: ImportHistoricalSmsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val statistics = getStatisticsUseCase()
                _uiState.value = DashboardUiState.Success(statistics)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun importHistoricalSms(days: Int = 30) {
        viewModelScope.launch {
            try {
                importHistoricalSmsUseCase(days) { processed, total ->
                    _uiState.value = DashboardUiState.Importing(processed, total)
                }
                loadStatistics()
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Import failed")
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val statistics: GetStatisticsUseCase.Statistics) : DashboardUiState()
    data class Importing(val processed: Int, val total: Int) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}