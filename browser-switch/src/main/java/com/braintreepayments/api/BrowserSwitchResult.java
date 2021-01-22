package com.braintreepayments.api;

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
        // TODO: unit test
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

    // TODO: determine if this has value for browser switch users
    public Uri getRequestUri() {
        return request.getUri();
    }

    public Uri getDeepLinkUri() {
        return deepLinkUri;
    }
}
