package com.braintreepayments.api

/**
 * Callback interface for Auth Tab results
 */
fun interface AuthTabCallback {
    /**
     * @param result The final result of the browser switch operation
     */
    fun onResult(result: BrowserSwitchFinalResult)
}