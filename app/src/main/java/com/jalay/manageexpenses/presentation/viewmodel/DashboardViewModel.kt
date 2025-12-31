package com.jalay.manageexpenses.presentation.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase
import com.jalay.manageexpenses.domain.usecase.ImportHistoricalSmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val importHistoricalSmsUseCase: ImportHistoricalSmsUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    companion object {
        private const val PREF_INITIAL_IMPORT_DONE = "initial_import_done"
    }

    init {
        checkAndLoadData()
    }

    private fun checkAndLoadData() {
        viewModelScope.launch {
            val isInitialImportDone = sharedPreferences.getBoolean(PREF_INITIAL_IMPORT_DONE, false)

            if (!isInitialImportDone) {
                // First time - need to import SMS data
                _uiState.value = DashboardUiState.InitialSetup
            } else {
                // Data already imported, just load statistics
                loadStatistics()
            }
        }
    }

    fun performInitialImport(days: Int? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = DashboardUiState.Importing(0, 0, isInitial = true)

                importHistoricalSmsUseCase(days) { processed, total ->
                    _uiState.value = DashboardUiState.Importing(processed, total, isInitial = true)
                }

                // Mark initial import as done
                sharedPreferences.edit().putBoolean(PREF_INITIAL_IMPORT_DONE, true).apply()

                loadStatistics()
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Import failed")
            }
        }
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

    fun refreshData() {
        // Just reload statistics without re-importing
        loadStatistics()
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    object InitialSetup : DashboardUiState()
    data class Success(val statistics: GetStatisticsUseCase.Statistics) : DashboardUiState()
    data class Importing(val processed: Int, val total: Int, val isInitial: Boolean = false) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
