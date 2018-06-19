package com.braintreepayments.browserswitch;

public enum BrowserSwitchResult {
    OK,
    CANCELED,
    ERROR;

    private String mErrorMessage;

    public String getErrorMessage() {
        return mErrorMessage;
    }

    BrowserSwitchResult setErrorMessage(String errorMessage) {
        mErrorMessage = errorMessage;
        return this;
    }

    @Override
    public String toString() {
        return name() + " " + getErrorMessage();
    }
}
