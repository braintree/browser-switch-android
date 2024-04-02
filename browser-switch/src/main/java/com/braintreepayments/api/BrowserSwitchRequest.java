package com.braintreepayments.api;

import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

// Links
// Base64 Encode a String in Android: https://stackoverflow.com/a/7360440

// TODO: consider encryption
// Ref: https://medium.com/fw-engineering/sharedpreferences-and-android-keystore-c4eac3373ac7

// TODO: Rename to `BrowserSwitchStartRequest` and remove `BrowserSwitchOptions` in favor this class
public class BrowserSwitchRequest {

    private static final String KEY_REQUEST_CODE = "requestCode";
    private static final String KEY_URL = "url";
    private static final String KEY_RETURN_URL_SCHEME = "returnUrlScheme";
    private static final String KEY_METADATA = "metadata";

    private final Uri url;
    private final int requestCode;
    private final JSONObject metadata;
    @VisibleForTesting
    final String returnUrlScheme;

    @NonNull
    static BrowserSwitchRequest fromBase64EncodedJSON(@NonNull String base64EncodedRequest) throws BrowserSwitchException {
        byte[] data = Base64.decode(base64EncodedRequest, Base64.DEFAULT);
        String requestJSONString = new String(data, StandardCharsets.UTF_8);
        try {
            JSONObject requestJSON = new JSONObject(requestJSONString);
            return new BrowserSwitchRequest(
                    requestJSON.getInt(KEY_REQUEST_CODE),
                    Uri.parse(requestJSON.getString(KEY_URL)),
                    requestJSON.optJSONObject(KEY_METADATA),
                    requestJSON.getString(KEY_RETURN_URL_SCHEME)
            );
        } catch (JSONException e) {
            throw new BrowserSwitchException("Unable to deserialize browser switch state.", e);
        }
    }

    BrowserSwitchRequest(int requestCode, Uri url, JSONObject metadata, String returnUrlScheme) {
        this.url = url;
        this.requestCode = requestCode;
        this.metadata = metadata;
        this.returnUrlScheme = returnUrlScheme;
    }

    Uri getUrl() {
        return url;
    }

    int getRequestCode() {
        return requestCode;
    }

    JSONObject getMetadata() {
        return metadata;
    }

    @NonNull
    String toBase64EncodedJSON() throws BrowserSwitchException {
        try {
            JSONObject requestJSON = new JSONObject()
                    .put(KEY_REQUEST_CODE, requestCode)
                    .put(KEY_URL, url.toString())
                    .put(KEY_RETURN_URL_SCHEME, returnUrlScheme)
                    .putOpt(KEY_METADATA, metadata);

            byte[] requestJSONBytes = requestJSON.toString().getBytes(StandardCharsets.UTF_8);
            return Base64.encodeToString(requestJSONBytes, Base64.DEFAULT);
        } catch (JSONException e) {
            throw new BrowserSwitchException("Unable to serialize browser switch state.", e);
        }
    }

    boolean matchesDeepLinkUrlScheme(@NonNull Uri url) {
        return url.getScheme() != null && url.getScheme().equalsIgnoreCase(returnUrlScheme);
    }
}
