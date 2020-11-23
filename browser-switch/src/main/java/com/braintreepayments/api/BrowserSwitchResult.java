package com.braintreepayments.api;

import androidx.annotation.IntDef;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BrowserSwitchResult {

    final private int status;
    final JSONObject requestMetadata;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ STATUS_OK, STATUS_CANCELED })
    public @interface BrowserSwitchStatus {}

    public static final int STATUS_OK = 1;
    public static final int STATUS_CANCELED = 2;

    BrowserSwitchResult(@BrowserSwitchStatus int status) {
        this(status, null);
    }

    BrowserSwitchResult(@BrowserSwitchStatus int status, JSONObject requestMetadata) {
        this.status = status;
        this.requestMetadata = requestMetadata;
    }

    @BrowserSwitchStatus
    public int getStatus() {
        return status;
    }

    public JSONObject getRequestMetadata() {
        return requestMetadata;
    }
}
