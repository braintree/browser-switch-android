package com.braintreepayments.api.browserswitch.demo.viewmodel

import com.braintreepayments.api.BrowserSwitchFinalResult

data class UiState (
    val browserSwitchStatusText: String? = null,
    val browserSwitchFinalResult: BrowserSwitchFinalResult? = null,
    val browserSwitchError: Exception? = null,
)