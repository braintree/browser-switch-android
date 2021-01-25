package com.braintreepayments.browserswitch;

import android.net.Uri;

import androidx.annotation.IntDef;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BrowserSwitchResult {

    private final int status;
    private final Uri deepLinkUri;
    private final BrowserSwitchRequest request;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_SUCCESS, STATUS_CANCELED})
    public @interface BrowserSwitchStatus {
    }

    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_CANCELED = 2;

    BrowserSwitchResult(@BrowserSwitchStatus int status, BrowserSwitchRequest request) {
        this(status, request, null);
    }

    BrowserSwitchResult(@BrowserSwitchStatus int status, BrowserSwitchRequest request, Uri deepLinkUri) {
        this.status = status;
        this.request = request;
        this.deepLinkUri = deepLinkUri;
    }

    @BrowserSwitchStatus
    public int getStatus() {
        return status;
    }

    public JSONObject getRequestMetadata() {
        return request.getMetadata();
    }

    public int getRequestCode() {
        return request.getRequestCode();
    }

    public Uri getRequestUrl() {
        return request.getUri();
    }

    public Uri getDeepLinkUrl() {
        return deepLinkUri;
    }
}
