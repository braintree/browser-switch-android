package com.braintreepayments.api

import android.net.Uri
import org.json.JSONObject

/**
 * The result of a browser switch obtained from [BrowserSwitchClient.parseResult]
 */
sealed class BrowserSwitchResult {

    /**
     * The browser switch was successfully completed. See [resultInfo] for details.
     */
    class Success(
        val deepLinkUrl: Uri,
        val requestCode: Int,
        val requestUrl: Uri,
        val requestMetadata: JSONObject?,
    ) : BrowserSwitchResult() {
        constructor(deepLinkUrl: Uri, originalRequest: BrowserSwitchRequest) : this(
            deepLinkUrl,
            originalRequest.requestCode,
            originalRequest.url,
            originalRequest.metadata
        )
    }

    /**
     * No browser switch result was found. This is the expected result when a user cancels the
     * browser switch flow without completing by closing the browser, or navigates back to the app
     * without completing the browser switch flow.
     */
    object NoResult : BrowserSwitchResult()

    /**
     * The browser switch failed.
     * @property [reason] Reason for the browser switch failure.
     */
    class Failure(val reason: BrowserSwitchException) : BrowserSwitchResult()
}
