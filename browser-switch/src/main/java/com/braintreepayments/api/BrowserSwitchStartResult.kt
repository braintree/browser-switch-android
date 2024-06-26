package com.braintreepayments.api

/**
 * The result of a browser switch obtained from [BrowserSwitchClient.completeRequest]
 */
sealed class BrowserSwitchStartResult {

    /**
     * The browser switch was successfully completed. See [resultInfo] for details.
     */
    class Success(val pendingRequest: String) : BrowserSwitchStartResult()

    /**
     * Browser switch failed with an [error].
     */
    class Failure(val error: Exception) : BrowserSwitchStartResult()
}
