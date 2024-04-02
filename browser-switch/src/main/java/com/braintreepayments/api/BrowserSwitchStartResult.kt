package com.braintreepayments.api

/**
 * A pending request for browser switching. This pending request should be stored locally within the app or
 * on-device and used to deliver a result of the browser flow in [BrowserSwitchClient.parseResult]
 */
sealed class BrowserSwitchStartResult {

    /**
     * Browser switch successfully started. Keep a reference to pending request state and pass it to
     * [BrowserSwitchClient.parseResult] after a deep link back into the app has occurred.
     */
    class Success(val pendingRequestState: String) : BrowserSwitchStartResult()

    /**
     * Browser switch failed with an [error].
     */
    class Failure(val error: Exception) : BrowserSwitchStartResult()
}
