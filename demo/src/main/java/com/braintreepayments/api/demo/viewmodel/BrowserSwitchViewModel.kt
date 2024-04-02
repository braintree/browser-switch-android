package com.braintreepayments.api.demo.viewmodel

import androidx.lifecycle.ViewModel
import com.braintreepayments.api.BrowserSwitchParseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BrowserSwitchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    var browserSwitchParseResult : BrowserSwitchParseResult.Success?
        get() = _uiState.value.browserSwitchParseResult
        set(value) {
            _uiState.update { it.copy(browserSwitchParseResult = value) }
            _uiState.update { it.copy(browserSwitchError = null) }
        }

    var browserSwitchError : Exception?
        get() = _uiState.value.browserSwitchError
        set(value) {
            _uiState.update { it.copy(browserSwitchError = value) }
            _uiState.update { it.copy(browserSwitchParseResult = null) }
        }
}