package com.braintreepayments.api

sealed class BrowserSwitchPendingRequest {

    class Started(val browserSwitchRequest: BrowserSwitchRequest) : BrowserSwitchPendingRequest()

    class Failure(val error: Exception) : BrowserSwitchPendingRequest()
}
