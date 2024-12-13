package com.braintreepayments.api

data class BrowserSwitchCallbacks(
    val onMinimized: (() -> Unit)? = null,
    val onFinished: (() -> Unit)? = null
)
