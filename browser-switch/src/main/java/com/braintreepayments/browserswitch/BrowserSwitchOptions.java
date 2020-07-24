package com.braintreepayments.browserswitch;

public class BrowserSwitchOptions {

    private int requestCode;
    private String url;

    public BrowserSwitchOptions requestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public BrowserSwitchOptions url(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }
}
