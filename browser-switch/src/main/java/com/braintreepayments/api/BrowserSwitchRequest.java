package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import org.json.JSONException;
import org.json.JSONObject;

public class BrowserSwitchRequest {

    private final Uri url;
    private final int requestCode;
    private final JSONObject metadata;
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public final String returnUrlScheme;
    private Uri appLinkUri;
    private boolean shouldNotifyCancellation;

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public static BrowserSwitchRequest fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int requestCode = jsonObject.getInt("requestCode");
        String url = jsonObject.getString("url");
        JSONObject metadata = jsonObject.optJSONObject("metadata");
        Uri appLinkUri = null;
        if (jsonObject.has("appLinkUri")) {
            appLinkUri = Uri.parse(jsonObject.getString("appLinkUri"));
        }
        String returnUrlScheme = null;
        if (jsonObject.has("returnUrlScheme")) {
            returnUrlScheme = jsonObject.getString("returnUrlScheme");
        }
        boolean shouldNotify = jsonObject.optBoolean("shouldNotify", true);
        return new BrowserSwitchRequest(
            requestCode,
            Uri.parse(url),
            metadata,
            returnUrlScheme,
            appLinkUri,
            shouldNotify
        );
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public BrowserSwitchRequest(
        int requestCode,
        Uri url,
        JSONObject metadata,
        String returnUrlScheme,
        Uri appLinkUri,
        boolean shouldNotifyCancellation
    ) {
        this.url = url;
        this.requestCode = requestCode;
        this.metadata = metadata;
        this.returnUrlScheme = returnUrlScheme;
        this.appLinkUri = appLinkUri;
        this.shouldNotifyCancellation = shouldNotifyCancellation;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public Uri getUrl() {
        return url;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public int getRequestCode() {
        return requestCode;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public JSONObject getMetadata() {
        return metadata;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public boolean getShouldNotifyCancellation() {
        return shouldNotifyCancellation;
    }

    void setShouldNotifyCancellation(boolean shouldNotifyCancellation) {
        this.shouldNotifyCancellation = shouldNotifyCancellation;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Nullable
    public Uri getAppLinkUri() {
        return appLinkUri;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void setAppLinkUri(@Nullable Uri appLinkUri) {
        this.appLinkUri = appLinkUri;
    }

    String toJson() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("requestCode", requestCode);
        result.put("url", url.toString());
        result.put("returnUrlScheme", returnUrlScheme);
        result.put("shouldNotify", shouldNotifyCancellation);
        if (metadata != null) {
            result.put("metadata", metadata);
        }
        if (appLinkUri != null) {
            result.put("appLinkUri", appLinkUri.toString());
        }
        return result.toString();
    }

    boolean matchesDeepLinkUrlScheme(@NonNull Uri url) {
        return url.getScheme() != null && url.getScheme().equalsIgnoreCase(returnUrlScheme);
    }

    boolean matchesAppLinkUri(@NonNull Uri uri) {
        return appLinkUri != null &&
            uri.getScheme().equals(appLinkUri.getScheme()) &&
            uri.getHost().equals(appLinkUri.getHost());
    }
}
