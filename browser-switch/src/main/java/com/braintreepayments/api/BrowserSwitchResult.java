package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The result of the browser switch.
 */
public class BrowserSwitchResult {

    private final int status;
    private final Uri deepLinkUrl;
    private final BrowserSwitchRequest request;

    static BrowserSwitchResult fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int status = jsonObject.getInt("status");
        String deepLinkUrl = jsonObject.getString("deepLinkUrl");
        String browserSwitchRequest = jsonObject.getString("browserSwitchRequest");
        return new BrowserSwitchResult(status, BrowserSwitchRequest.fromJson(browserSwitchRequest), Uri.parse(deepLinkUrl));
    }

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

    public String toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("status", status);
        result.put("deepLinkUrl", deepLinkUrl.toString());
        result.put("browserSwitchRequest", request.toJson());
        return result.toString();
    }
}
