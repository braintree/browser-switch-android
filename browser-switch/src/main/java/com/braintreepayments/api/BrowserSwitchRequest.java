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
    static BrowserSwitchRequest fromToken(@NonNull String tokenBase64) throws BrowserSwitchException {
        byte[] data = Base64.decode(tokenBase64, Base64.DEFAULT);
        String token = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject tokenJSON = new JSONObject(token);
            return new BrowserSwitchRequest(
                    tokenJSON.getInt(KEY_REQUEST_CODE),
                    Uri.parse(tokenJSON.getString(KEY_URL)),
                    tokenJSON.getJSONObject(KEY_METADATA),
                    tokenJSON.getString(KEY_RETURN_URL_SCHEME)
            );
        } catch (JSONException e) {
            throw new BrowserSwitchException("Unable to decode browser switch state from token.", e);
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
    String tokenize() throws BrowserSwitchException {
        try {
            // TODO: make return url scheme accessor public
            JSONObject tokenJSON = new JSONObject()
                    .put(KEY_REQUEST_CODE, requestCode)
                    .put(KEY_URL, url.toString())
                    .put(KEY_RETURN_URL_SCHEME, returnUrlScheme)
                    .putOpt(KEY_METADATA, metadata);

            byte[] tokenBytes = tokenJSON.toString().getBytes(StandardCharsets.UTF_8);
            return Base64.encodeToString(tokenBytes, Base64.DEFAULT);
        } catch (JSONException e) {
            throw new BrowserSwitchException("Unable to tokenize Browser Switch State", e);
        }
    }

    boolean matchesDeepLinkUrlScheme(@NonNull Uri url) {
        return url.getScheme() != null && url.getScheme().equalsIgnoreCase(returnUrlScheme);
    }
}
