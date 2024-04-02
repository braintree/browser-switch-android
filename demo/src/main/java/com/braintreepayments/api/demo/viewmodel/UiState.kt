package com.braintreepayments.api.demo.viewmodel

import com.braintreepayments.api.BrowserSwitchParseResult

data class UiState(
    val browserSwitchParseResult: BrowserSwitchParseResult.Success? = null,
    val browserSwitchError: Exception? = null,
)