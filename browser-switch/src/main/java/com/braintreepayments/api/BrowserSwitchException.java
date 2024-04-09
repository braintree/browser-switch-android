package com.braintreepayments.api;

import androidx.annotation.VisibleForTesting;

/**
 * Error class thrown when browser switch returns an error.
 */
public class BrowserSwitchException extends Exception {

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public BrowserSwitchException(String message) {
        super(message);
    }
}
