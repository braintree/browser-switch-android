package com.braintreepayments.api

/**
 * The result of a browser switch obtained from [BrowserSwitchClient.start]
 */
@Suppress("LibraryEntitiesShouldNotBePublic")
sealed class BrowserSwitchStartResult {

    /**
     * The browser switch was successfully completed. Store pendingRequest String to complete
     * browser switch after deeplinking back into the application (see [BrowserSwitchClient.completeRequest]).
     */
    class Started(val pendingRequest: String) : BrowserSwitchStartResult()

    /**
     * Browser switch failed with an [error].
     */
    class Failure(val error: Exception) : BrowserSwitchStartResult()
}
