package com.braintreepayments.browserswitch;

import android.net.Uri;

import org.json.JSONObject;

public class BrowserSwitchOptions {

    private int requestCode;
    private Uri url;
    private JSONObject metadata;

    public BrowserSwitchOptions metadata(JSONObject metadata) {
        this.metadata = metadata;
        return this;
    }

    public BrowserSwitchOptions requestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public BrowserSwitchOptions url(Uri url) {
        this.url = url;
        return this;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public Uri getUrl() {
        return url;
    }
}
