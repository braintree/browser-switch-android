package com.braintreepayments.api

sealed class BrowserSwitchResult {

    class Success(val resultInfo: BrowserSwitchResultInfo) : BrowserSwitchResult()

    object NoResult : BrowserSwitchResult()
}
