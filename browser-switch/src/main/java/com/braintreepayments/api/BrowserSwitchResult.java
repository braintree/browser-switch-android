package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONObject;

/**
 * The result of the browser switch.
 */
public class BrowserSwitchResult {

    private final int status;
    private final Uri deepLinkUrl;
    private final BrowserSwitchRequest request;

    BrowserSwitchResult(@BrowserSwitchStatus int status, BrowserSwitchRequest request) {
        this(status, request, null);
    }

    BrowserSwitchResult(@BrowserSwitchStatus int status, BrowserSwitchRequest request, Uri deepLinkUrl) {
        this.status = status;
        this.request = request;
        this.deepLinkUrl = deepLinkUrl;
    }

    /**
     * @return The {@link BrowserSwitchStatus} of the browser switch
     */
    @BrowserSwitchStatus
    public int getStatus() {
        return status;
    }

    /**
     * @return A {@link JSONObject} containing metadata persisted through the browser switch
     */
    @Nullable
    public JSONObject getRequestMetadata() {
        return request.getMetadata();
    }

    /**
     * @return Request code int to associate with the browser switch request
     */
    public int getRequestCode() {
        return request.getRequestCode();
    }

    // TODO: Make url and uri variable and method naming consistent
    /**
     * @return The target url used to initiate the browser switch
     */
    @Nullable
    public Uri getRequestUrl() {
        return request.getUrl();
    }

    /**
     * @return The return url used for deep linking back into the application after browser switch
     */
    @Nullable
    public Uri getDeepLinkUrl() {
        return deepLinkUrl;
    }
}
