package com.braintreepayments.browserswitch;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

class BrowserSwitchRequest {

    private Uri uri;
    private String state;
    private int requestCode;

    static final String PENDING = "PENDING";
    static final String SUCCESS = "SUCCESS";

    static BrowserSwitchRequest fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int requestCode = jsonObject.getInt("requestCode");
        String url = jsonObject.getString("url");
        String state = jsonObject.getString("state");
        return new BrowserSwitchRequest(requestCode, Uri.parse(url), state);
    }

    BrowserSwitchRequest(int requestCode, Uri uri, String state) {
        this.uri = uri;
        this.state = state;
        this.requestCode = requestCode;
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

    void setState(String state) {
        this.state = state;
    }

    String toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("requestCode", requestCode);
        result.put("url", uri.toString());
        result.put("state", state);
        return result.toString();
    }
}

