package com.braintreepayments.api;

/**
 * Implement this interface to receive notifications from a {@link BrowserSwitchObserver} that a
 * browser switch has occurred.
 */
public interface BrowserSwitchListener {
    void onBrowserSwitchResult(BrowserSwitchResult result);
}
