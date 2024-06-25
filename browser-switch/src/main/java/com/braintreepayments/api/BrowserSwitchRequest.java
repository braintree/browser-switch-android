package com.braintreepayments.api;

import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class BrowserSwitchRequest {

    private static final String KEY_REQUEST_CODE = "requestCode";
    private static final String KEY_URL = "url";
    private static final String KEY_RETURN_URL_SCHEME = "returnUrlScheme";
    private static final String KEY_METADATA = "metadata";
    private static final String KEY_APP_LINK_URI = "appLinkUri";

    private final Uri url;
    private final int requestCode;
    private final JSONObject metadata;
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public final String returnUrlScheme;
    private Uri appLinkUri;

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
        return new BrowserSwitchRequest(
            requestCode,
            Uri.parse(url),
            metadata,
            returnUrlScheme,
            appLinkUri
        );
    }

    @NonNull
    static BrowserSwitchRequest fromBase64EncodedJSON(@NonNull String base64EncodedRequest) throws BrowserSwitchException {
        byte[] data = Base64.decode(base64EncodedRequest, Base64.DEFAULT);
        String requestJSONString = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject requestJSON = new JSONObject(requestJSONString);

            Uri appLinkUri = null;
            if (requestJSON.has(KEY_APP_LINK_URI)) {
                appLinkUri = Uri.parse(requestJSON.getString(KEY_APP_LINK_URI));
            }
            return new BrowserSwitchRequest(
                    requestJSON.getInt(KEY_REQUEST_CODE),
                    Uri.parse(requestJSON.getString(KEY_URL)),
                    requestJSON.optJSONObject(KEY_METADATA),
                    requestJSON.optString(KEY_RETURN_URL_SCHEME),
                    appLinkUri
            );
        } catch (JSONException e) {
            throw new BrowserSwitchException("Unable to deserialize browser switch state.", e);
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public BrowserSwitchRequest(
        int requestCode,
        Uri url,
        JSONObject metadata,
        String returnUrlScheme,
        Uri appLinkUri
    ) {
        this.url = url;
        this.requestCode = requestCode;
        this.metadata = metadata;
        this.returnUrlScheme = returnUrlScheme;
        this.appLinkUri = appLinkUri;
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
        if (metadata != null) {
            result.put("metadata", metadata);
        }
        if (appLinkUri != null) {
            result.put("appLinkUri", appLinkUri.toString());
        }
        return result.toString();
    }

    @NonNull
    String toBase64EncodedJSON() throws BrowserSwitchException {
        try {
            JSONObject requestJSON = new JSONObject()
                    .put(KEY_REQUEST_CODE, requestCode)
                    .put(KEY_URL, url.toString())
                    .putOpt(KEY_RETURN_URL_SCHEME, returnUrlScheme)
                    .putOpt(KEY_METADATA, metadata)
                    .putOpt(KEY_APP_LINK_URI, appLinkUri);

            byte[] requestJSONBytes = requestJSON.toString().getBytes(StandardCharsets.UTF_8);
            return Base64.encodeToString(requestJSONBytes, Base64.DEFAULT);
        } catch (JSONException e) {
            throw new BrowserSwitchException("Unable to serialize browser switch state.", e);
        }
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
