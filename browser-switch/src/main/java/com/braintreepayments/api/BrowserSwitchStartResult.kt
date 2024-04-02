package com.braintreepayments.api

/**
 * A pending request for browser switching. This pending request should be stored locally within the app or
 * on-device and used to deliver a result of the browser flow in [BrowserSwitchClient.parseResult]
 */
sealed class BrowserSwitchStartResult {

    /**
     * A browser switch was successfully started. This pending request should be store dnd passed to
     * [BrowserSwitchClient.parseResult]
     */
    class Success(val pendingRequestState: String) : BrowserSwitchStartResult()

    /**
     * An error with [cause] occurred launching the browser
     */
    class Failure(val cause: Exception) : BrowserSwitchStartResult()
}
