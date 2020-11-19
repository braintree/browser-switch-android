package com.braintreepayments.api;

import androidx.annotation.IntDef;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BrowserSwitchResult {

    final private int status;
    final private String errorMessage;
    final JSONObject requestMetadata;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ STATUS_OK, STATUS_CANCELED, STATUS_ERROR })
    public @interface BrowserSwitchStatus {}

    public static final int STATUS_OK = 1;
    public static final int STATUS_CANCELED = 2;
    public static final int STATUS_ERROR = 3;

    BrowserSwitchResult(@BrowserSwitchStatus int status, String errorMessage) {
        this(status, errorMessage, null);
    }

    BrowserSwitchResult(@BrowserSwitchStatus int status, String errorMessage, JSONObject requestMetadata) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.requestMetadata = requestMetadata;
    }

    @BrowserSwitchStatus
    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public JSONObject getRequestMetadata() {
        return requestMetadata;
    }
}
