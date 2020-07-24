package com.braintreepayments.browserswitch;

import android.content.Intent;
import android.net.Uri;

import org.json.JSONObject;

public class BrowserSwitchOptions {

    private Intent intent;
    private JSONObject metadata;
    private int requestCode;
    private Uri url;

    public BrowserSwitchOptions intent(Intent intent) {
        this.intent = intent;
        return this;
    }

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

    public Intent getIntent() {
        return intent;
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
