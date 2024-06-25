package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Details of a successful {@link BrowserSwitchStartResult}
 */
public class BrowserSwitchResultInfo {

    private static final String KEY_DEEP_LINK_URL = "deepLinkUrl";
    private static final String KEY_BROWSER_SWITCH_REQUEST = "browserSwitchRequest";

    private final Uri deepLinkUrl;
    private final BrowserSwitchRequest request;

    static BrowserSwitchResultInfo fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        String deepLinkUrl = jsonObject.getString(KEY_DEEP_LINK_URL);
        String browserSwitchRequest = jsonObject.getString(KEY_BROWSER_SWITCH_REQUEST);
        return new BrowserSwitchResultInfo(BrowserSwitchRequest.fromJson(browserSwitchRequest), Uri.parse(deepLinkUrl));
    }

    BrowserSwitchResultInfo(BrowserSwitchRequest request, Uri deepLinkUrl) {
        this.request = request;
        this.deepLinkUrl = deepLinkUrl;
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
        result.put(KEY_DEEP_LINK_URL, deepLinkUrl.toString());
        result.put(KEY_BROWSER_SWITCH_REQUEST, request.toJson());
        return result.toString();
    }
}
