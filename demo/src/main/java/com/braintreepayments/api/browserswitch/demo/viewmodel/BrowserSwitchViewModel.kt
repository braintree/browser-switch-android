package com.braintreepayments.api.browserswitch.demo.viewmodel

import androidx.lifecycle.ViewModel
import com.braintreepayments.api.BrowserSwitchFinalResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BrowserSwitchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    var browserSwitchStatusText: String?
        get() = _uiState.value.browserSwitchStatusText
        set(value) {
            _uiState.update { it.copy(browserSwitchStatusText = value) }
        }

    var browserSwitchFinalResult: BrowserSwitchFinalResult?
        get() = _uiState.value.browserSwitchFinalResult
        set(value) {
            _uiState.update { it.copy(browserSwitchFinalResult = value) }
            _uiState.update { it.copy(browserSwitchError = null) }
        }

    var browserSwitchError: Exception?
        get() = _uiState.value.browserSwitchError
        set(value) {
            _uiState.update { it.copy(browserSwitchError = value) }
            _uiState.update { it.copy(browserSwitchFinalResult = null) }
        }
}