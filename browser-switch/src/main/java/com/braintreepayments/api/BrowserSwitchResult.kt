package com.braintreepayments.api

/**
 * The result of a browser switch obtained from [BrowserSwitchClient.parseResult]
 */
sealed class BrowserSwitchResult {

    /**
     * The browser switch was successfully completed. See [resultInfo] for details.
     */
    class Success(val resultInfo: BrowserSwitchResultInfo) : BrowserSwitchResult()

    /**
     * No browser switch result was found. This is the expected result when a user cancels the
     * browser switch flow without completing by closing the browser, or navigates back to the app
     * without completing the browser switch flow.
     */
    object NoResult : BrowserSwitchResult()
}