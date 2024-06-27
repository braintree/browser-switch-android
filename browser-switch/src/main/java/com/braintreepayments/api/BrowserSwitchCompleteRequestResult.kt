package com.braintreepayments.api

import android.net.Uri
import org.json.JSONObject

/**
 * The result of a browser switch obtained from [BrowserSwitchClient.completeRequest]
 */
sealed class BrowserSwitchCompleteRequestResult {

    /**
     * The browser switch was successfully completed.
     */
    class Success internal constructor(
        val deepLinkUrl: Uri,
        val requestCode: Int,
        val requestUrl: Uri,
        val requestMetadata: JSONObject?,
    ) : BrowserSwitchCompleteRequestResult() {
        internal constructor(deepLinkUrl: Uri, originalRequest: BrowserSwitchRequest) : this(
            deepLinkUrl,
            originalRequest.requestCode,
            originalRequest.url,
            originalRequest.metadata
        )
    }

    /**
     * The browser switch failed.
     * @property [error] Error detailing the reason for the browser switch failure.
     */
    class Failure internal constructor(val error: BrowserSwitchException) :
        BrowserSwitchCompleteRequestResult()

    /**
     * No browser switch result was found. This is the expected result when a user cancels the
     * browser switch flow without completing by closing the browser, or navigates back to the app
     * without completing the browser switch flow.
     */
    object NoResult : BrowserSwitchCompleteRequestResult()
}
