package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONObject;

/**
 * Object that contains a set of browser switch parameters for use with
 * {@link BrowserSwitchClient#start(android.app.Activity, BrowserSwitchOptions)}.
 */
public class BrowserSwitchOptions {

    private JSONObject metadata;
    private int requestCode;
    private Uri url;
    private String returnUrlScheme;
    private Uri appLinkUri;
    private Uri successAppLinkUri;

    private boolean launchAsNewTask;
    private LaunchType launchType;

    /**
     * Set browser switch metadata.
     *
     * @param metadata JSONObject containing metadata that will be persisted and returned in a
     *                 {@link BrowserSwitchFinalResult} when the app has re-entered the foreground
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions metadata(@Nullable JSONObject metadata) {
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
    public BrowserSwitchOptions url(@Nullable Uri url) {
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
    public BrowserSwitchOptions returnUrlScheme(@Nullable String returnUrlScheme) {
        this.returnUrlScheme = returnUrlScheme;
        return this;
    }

    /**
     * Set App Link [Uri].
     *
     * @param appLinkUri The [Uri] containing the App Link URL used for navigating back into the application
     *                   after browser switch
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions appLinkUri(@Nullable Uri appLinkUri) {
        this.appLinkUri = appLinkUri;
        return this;
    }
    /**
     * Set Success App Link [Uri].
     *
     * @param successAppLinkUri The [Uri] containing the App Link URL with full success path
     *                          used for navigating back into the application after browser switch
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions successAppLinkUri(@Nullable Uri successAppLinkUri) {
        this.successAppLinkUri = successAppLinkUri;
        return this;
    }

    /**
     * @return The metadata associated with the browser switch request
     */
    @Nullable
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
    @Nullable
    public Uri getUrl() {
        return url;
    }

    /**
     * @return The return url scheme for deep linking back into the application after browser switch
     */
    @Nullable
    public String getReturnUrlScheme() {
        return returnUrlScheme;
    }

    /**
     * @return The App Link [Uri] set for navigating back into the application after browser switch
     */
    @Nullable
    public Uri getAppLinkUri() {
        return appLinkUri;
    }

    /**
     * @return The Success App Link [Uri] set for navigating back into the application
     *                              after browser switch containing the full path back
     */
    @Nullable
    public Uri getSuccessAppLinkUri() {
        return successAppLinkUri;
    }

    /**
     * @deprecated Use {@link #getLaunchType()} instead.
     */
    @Deprecated
    public boolean isLaunchAsNewTask() {
        return launchAsNewTask;
    }

    /**
     * @deprecated Use {@link #launchType(LaunchType)} instead.
     */
    @Deprecated
    public BrowserSwitchOptions launchAsNewTask(boolean launchAsNewTask) {
        this.launchAsNewTask = launchAsNewTask;
        this.launchType = LaunchType.ACTIVITY_NEW_TASK;
        return this;
    }

    /**
     * @return the activity launch type flag.
     */
    public LaunchType getLaunchType() {
        return launchType;
    }

    /**
     * Sets the activity launch type flag.
     *
     * @param launchType the type of launch for the browser activity
     * @return {@link BrowserSwitchOptions} reference to instance to allow setter invocations to be chained
     */
    public BrowserSwitchOptions launchType(LaunchType launchType) {
        this.launchType = launchType;
        return this;
    }
}
