package com.braintreepayments.api.demo.viewmodel

import com.braintreepayments.api.BrowserSwitchResult

data class UiState(
    val browserSwitchResult: BrowserSwitchResult.Success? = null,
    val browserSwitchError: Exception? = null,
)