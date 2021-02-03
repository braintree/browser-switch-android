package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.IntDef;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The result of the browser switch.
 */
public class BrowserSwitchResult {

    private final int status;
    private final Uri deepLinkUri;
    private final BrowserSwitchRequest request;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BrowserSwitchStatus.SUCCESS, BrowserSwitchStatus.CANCELED})
    public @interface BrowserSwitchStatus {
        /**
         * Browser switch is considered a success when a user is deep linked back into the app.
         */
        int SUCCESS = 1;

        /**
         * Browser switch is considered canceled when a user re-enters the app without deep link.
         */
        int CANCELED = 2;
    }

    BrowserSwitchResult(@BrowserSwitchStatus int status, BrowserSwitchRequest request) {
        this(status, request, null);
    }

    BrowserSwitchResult(@BrowserSwitchStatus int status, BrowserSwitchRequest request, Uri deepLinkUri) {
        this.status = status;
        this.request = request;
        this.deepLinkUri = deepLinkUri;
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
    public JSONObject getRequestMetadata() {
        return request.getMetadata();
    }

    /**
     * @return Request code int to associate with the browser switch request
     */
    public int getRequestCode() {
        return request.getRequestCode();
    }

    /**
     * @return The target url used to initiate the browser switch
     */
    public Uri getRequestUrl() {
        return request.getUri();
    }

    /**
     * @return The return url used for deep linking back into the application after browser switch
     */                      
    public Uri getDeepLinkUrl() {
        return deepLinkUri;
    }
}
