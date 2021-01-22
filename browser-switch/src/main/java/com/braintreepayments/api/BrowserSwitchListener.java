package com.braintreepayments.api;

public interface BrowserSwitchListener {
    /**
     * The result of a browser switch will be returned in this method.
     *  @param result The {@link BrowserSwitchResult}. It will have a status of either
     * {@link BrowserSwitchResult#STATUS_SUCCESS} or {@link BrowserSwitchResult#STATUS_CANCELED}.
     */
    void onBrowserSwitchResult(BrowserSwitchResult result);
}
