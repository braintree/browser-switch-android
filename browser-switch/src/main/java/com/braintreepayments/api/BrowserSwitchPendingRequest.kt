package com.braintreepayments.api

/**
 * A pending request for browser switching. This pending request should be stored locally within the app or
 * on-device and used to deliver a result of the browser flow in [BrowserSwitchClient.parseResult]
 */
sealed class BrowserSwitchPendingRequest {

    /**
     * A browser switch was successfully started. This pending request should be store dnd passed to
     * [BrowserSwitchClient.parseResult]
    */
    class Started(val browserSwitchRequest: BrowserSwitchRequest) : BrowserSwitchPendingRequest() {

        /**
         * Convenience constructor to create a [BrowserSwitchPendingRequest.Started] from your stored
         * [String] from [BrowserSwitchPendingRequest.Started.toJsonString]
         */
        constructor(jsonString: String) : this(BrowserSwitchRequest.fromJson(jsonString))

        /**
         * Convenience method to return [BrowserSwitchPendingRequest.Started] in [String] format to be
         * persisted in storage
         */
        fun toJsonString(): String {
            return browserSwitchRequest.toJson()
        }
    }

    /**
     * An error with [cause] occurred launching the browser
     */
    class Failure(val cause: Exception) : BrowserSwitchPendingRequest()
}
