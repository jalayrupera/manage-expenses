package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.domain.usecase.ExportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun export(
        format: ExportDataUseCase.ExportFormat,
        startTime: Long? = null,
        endTime: Long? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ExportUiState.Exporting

            val result = exportDataUseCase(format, startTime, endTime)

            result.onSuccess { path ->
                _uiState.value = ExportUiState.Success(path)
            }.onFailure { error ->
                _uiState.value = ExportUiState.Error(error.message ?: "Export failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = ExportUiState.Idle
    }
}

sealed class ExportUiState {
    object Idle : ExportUiState()
    object Exporting : ExportUiState()
    data class Success(val filePath: String) : ExportUiState()
    data class Error(val message: String) : ExportUiState()
}
