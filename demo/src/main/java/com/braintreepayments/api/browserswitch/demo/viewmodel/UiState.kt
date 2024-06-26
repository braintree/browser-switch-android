package com.braintreepayments.api.browserswitch.demo.viewmodel

import com.braintreepayments.api.BrowserSwitchCompleteRequestResult

data class UiState (
    val browserSwitchResult: BrowserSwitchCompleteRequestResult? = null,
    val browserSwitchError: Exception? = null,
)