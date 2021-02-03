package com.braintreepayments.api;

/**
 * Error class thrown when browser switch returns an error.
 */
public class BrowserSwitchException extends Exception {

    BrowserSwitchException(String message) {
        super(message);
    }
}
