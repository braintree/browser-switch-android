package com.braintreepayments.api;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

class BrowserSwitchRequest {

    final private Uri uri;
    final private int requestCode;
    final private JSONObject metadata;

    static BrowserSwitchRequest fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int requestCode = jsonObject.getInt("requestCode");
        String url = jsonObject.getString("url");
        JSONObject metadata = jsonObject.optJSONObject("metadata");
        return new BrowserSwitchRequest(requestCode, Uri.parse(url), metadata);
    }

    BrowserSwitchRequest(int requestCode, Uri uri, JSONObject metadata) {
        this.uri = uri;
        this.requestCode = requestCode;
        this.metadata = metadata;
    }

    Uri getUri() {
        return uri;
    }

    int getRequestCode() {
        return requestCode;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    String toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("requestCode", requestCode);
        result.put("url", uri.toString());
        if (metadata != null) {
            result.put("metadata", metadata);
        }
        return result.toString();
    }
}
