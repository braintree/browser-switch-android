package com.braintreepayments.api.demo.viewmodel

import androidx.lifecycle.ViewModel
import com.braintreepayments.api.BrowserSwitchClient
import com.braintreepayments.api.BrowserSwitchPendingRequest
import com.braintreepayments.api.BrowserSwitchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BrowserSwitchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    var browserSwitchResult : BrowserSwitchResult?
        get() = _uiState.value.browserSwitchResult
        set(value) {
            _uiState.update { it.copy(browserSwitchResult = value) }
            _uiState.update { it.copy(browserSwitchError = null) }
        }

    var browserSwitchError : Exception?
        get() = _uiState.value.browserSwitchError
        set(value) {
            _uiState.update { it.copy(browserSwitchError = value) }
            _uiState.update { it.copy(browserSwitchResult = null) }
        }
}