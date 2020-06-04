package com.braintreepayments.browserswitch;

public class BrowserSwitchResult {

    final private int status;
    final private String errorMessage;

    public static final int STATUS_OK = 1;
    public static final int STATUS_CANCELED = 2;
    public static final int STATUS_ERROR = 3;

    BrowserSwitchResult(int status) {
        this(status, null);
    }

    BrowserSwitchResult(int status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
