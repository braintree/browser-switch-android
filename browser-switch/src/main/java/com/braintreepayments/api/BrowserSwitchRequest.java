package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

class BrowserSwitchRequest {

    private final Uri url;
    private final int requestCode;
    private final JSONObject metadata;

    private final String returnUrlScheme;
    private final String pendingId;

    static BrowserSwitchRequest fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int requestCode = jsonObject.getInt("requestCode");
        String url = jsonObject.getString("url");
        String returnUrlScheme = jsonObject.getString("returnUrlScheme");
        String pendingId = jsonObject.getString("pendingId");
        JSONObject metadata = jsonObject.optJSONObject("metadata");
        return new BrowserSwitchRequest(requestCode, Uri.parse(url), metadata, returnUrlScheme, pendingId);
    }

    static String createUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    BrowserSwitchRequest(int requestCode, Uri url, JSONObject metadata, String returnUrlScheme) {
        this(requestCode, url, metadata, returnUrlScheme, createUUID());
    }

    private BrowserSwitchRequest(int requestCode, Uri url, JSONObject metadata, String returnUrlScheme, String pendingId) {
        this.url = url;
        this.requestCode = requestCode;
        this.metadata = metadata;
        this.returnUrlScheme = returnUrlScheme;
        this.pendingId = pendingId;
    }

    Uri getUrl() {
        return url;
    }

    int getRequestCode() {
        return requestCode;
    }

    JSONObject getMetadata() {
        return metadata;
    }

    public String getReturnUrlScheme() {
        return returnUrlScheme;
    }

    String toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("requestCode", requestCode);
        result.put("url", url.toString());
        result.put("pendingId", pendingId);
        result.put("returnUrlScheme", returnUrlScheme);
        if (metadata != null) {
            result.put("metadata", metadata);
        }
        return result.toString();
    }

    boolean matchesDeepLinkUrlScheme(@NonNull Uri url) {
        return url.getScheme().equals(returnUrlScheme);
    }

    public String getPendingId() {
        return pendingId;
    }
}
