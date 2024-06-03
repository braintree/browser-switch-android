package com.braintreepayments.api.browserswitch.demo.viewmodel

import com.braintreepayments.api.BrowserSwitchResultInfo

data class UiState (
    val browserSwitchResult: BrowserSwitchResultInfo? = null,
    val browserSwitchError: Exception? = null,
)