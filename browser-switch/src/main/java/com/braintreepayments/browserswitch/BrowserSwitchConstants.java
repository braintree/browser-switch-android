package com.braintreepayments.browserswitch;

public class BrowserSwitchConstants {

    /**
     * Having a single, static id helps enforce the one pending request at a time feature.
     */
    public static final long PENDING_REQUEST_ID = 1;

    private BrowserSwitchConstants() {
        throw new AssertionError("BrowserSwitchConstants should not be instantiated.");
    };
}
