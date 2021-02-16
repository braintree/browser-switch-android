package com.braintreepayments.api;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

class BrowserSwitchRequest {

    private final Uri url;
    private final int requestCode;
    private final JSONObject metadata;

    static BrowserSwitchRequest fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int requestCode = jsonObject.getInt("requestCode");
        String url = jsonObject.getString("url");
        JSONObject metadata = jsonObject.optJSONObject("metadata");
        return new BrowserSwitchRequest(requestCode, Uri.parse(url), metadata);
    }

    BrowserSwitchRequest(int requestCode, Uri url, JSONObject metadata) {
        this.url = url;
        this.requestCode = requestCode;
        this.metadata = metadata;
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

    String toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("requestCode", requestCode);
        result.put("url", url.toString());
        if (metadata != null) {
            result.put("metadata", metadata);
        }
        return result.toString();
    }
}
