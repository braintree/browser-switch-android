package com.braintreepayments.api;

import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

/**
 * Object that contains a set of browser switch parameters for use with
 * {@link BrowserSwitchClient#start(FragmentActivity, BrowserSwitchOptions)}.
 */
public class BrowserSwitchOptions {

    private JSONObject metadata;
    private int requestCode;
    private Uri url;
    private String returnUrlScheme;

    /**
     * Set browser switch metadata.
     *
     * @param metadata JSONObject containing metadata that will be persisted and returned in a
     *                 {@link BrowserSwitchResult} when the app has re-entered the foreground
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions metadata(JSONObject metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Set browser switch request code.
     *
     * @param requestCode Request code int to associate with the browser switch request
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions requestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    /**
     * Set browser switch url.
     *
     * @param url The target url to use for browser switch
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions url(Uri url) {
        this.url = url;
        return this;
    }

    /**
     * Set browser switch return url scheme.
     *
     * @param returnUrlScheme The return url scheme to use for deep linking back into the application
     *                        after browser switch
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions returnUrlScheme(String returnUrlScheme) {
        this.returnUrlScheme = returnUrlScheme;
        return this;
    }

    /**
     * @return The metadata associated with the browser switch request
     */
    public JSONObject getMetadata() {
        return metadata;
    }

    /**
     * @return The request code associated with the browser switch request
     */
    public int getRequestCode() {
        return requestCode;
    }

    /**
     * @return The target url used for browser switch
     */
    public Uri getUrl() {
        return url;
    }

    /**
     * @return The return url scheme for deep linking back into the application after browser switch
     */
    public String getReturnUrlScheme() {
        return returnUrlScheme;
    }
}
