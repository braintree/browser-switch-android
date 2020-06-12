package com.braintreepayments.browserswitch;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BrowserSwitchResult {

    final private int status;
    final private String errorMessage;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ STATUS_OK, STATUS_CANCELED, STATUS_ERROR })
    public @interface BrowserSwitchStatus {}

    public static final int STATUS_OK = 1;
    public static final int STATUS_CANCELED = 2;
    public static final int STATUS_ERROR = 3;

    BrowserSwitchResult(@BrowserSwitchStatus int status) {
        this(status, null);
    }

    BrowserSwitchResult(@BrowserSwitchStatus int status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    @BrowserSwitchStatus
    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
