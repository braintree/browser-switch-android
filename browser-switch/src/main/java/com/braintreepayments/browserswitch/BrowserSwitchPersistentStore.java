package com.braintreepayments.browserswitch;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.json.JSONException;

class BrowserSwitchPersistentStore {

    @VisibleForTesting
    static final String REQUEST_KEY = "browserSwitch.request";

    private static final BrowserSwitchPersistentStore INSTANCE = new BrowserSwitchPersistentStore();

    static BrowserSwitchPersistentStore getInstance() {
        return INSTANCE;
    }

    private BrowserSwitchPersistentStore() {}

    BrowserSwitchRequest getActiveRequest(Context context) {
        BrowserSwitchRequest request = null;

        String activeRequestJson = PersistentStore.get(REQUEST_KEY, context);
        if (activeRequestJson != null) {
            try {
                request = BrowserSwitchRequest.fromJson(activeRequestJson);
            } catch (JSONException ignored) { /* do nothing */ }
        }
        return request;
    }

    void putActiveRequest(BrowserSwitchRequest request, Context context) {
        try {
            PersistentStore.put(REQUEST_KEY, request.toJson(), context);
        } catch (JSONException ignored) { /* do nothing */ }
    }

    void clearActiveRequest(Context context) {
        PersistentStore.remove(REQUEST_KEY, context);
    }
}
