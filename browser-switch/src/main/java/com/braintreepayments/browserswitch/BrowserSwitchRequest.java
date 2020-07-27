package com.braintreepayments.browserswitch;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

class BrowserSwitchRequest {

    private Uri uri;
    private String state;
    final private int requestCode;
    private JSONObject metadata;

    static final String PENDING = "PENDING";
    static final String SUCCESS = "SUCCESS";

    static BrowserSwitchRequest fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int requestCode = jsonObject.getInt("requestCode");
        String url = jsonObject.getString("url");
        String state = jsonObject.getString("state");
        JSONObject metadata = jsonObject.optJSONObject("metadata");
        return new BrowserSwitchRequest(requestCode, Uri.parse(url), state, metadata);
    }

    BrowserSwitchRequest(int requestCode, Uri uri, String state, JSONObject metadata) {
        this.uri = uri;
        this.state = state;
        this.requestCode = requestCode;
        this.metadata = metadata;
    }

    void setUri(Uri value) {
        uri = value;
    }

    Uri getUri() {
        return uri;
    }

    int getRequestCode() {
        return requestCode;
    }

    String getState() {
        return state;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JSONObject metadata) {
        this.metadata = metadata;
    }

    void setState(String state) {
        this.state = state;
    }

    String toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("requestCode", requestCode);
        result.put("url", uri.toString());
        result.put("state", state);
        if (metadata != null) {
            result.put("metadata", metadata);
        }
        return result.toString();
    }
}

