package com.braintreepayments.browserswitch;

import android.content.Context;

import org.json.JSONException;

class BrowserSwitchPersistentStore {

    private static final BrowserSwitchPersistentStore INSTANCE = new BrowserSwitchPersistentStore();

    static BrowserSwitchPersistentStore getInstance() {
        return INSTANCE;
    }

    private BrowserSwitchPersistentStore() {}

    BrowserSwitchRequest getActiveRequest(Context context) {
        BrowserSwitchRequest request = null;

        String activeRequestJson = PersistentStore.get("browserSwitch", context);
        if (activeRequestJson != null) {
            try {
                request = BrowserSwitchRequest.fromJson(activeRequestJson);
            } catch (JSONException ignored) { /* do nothing */ }
        }
        return request;
    }

    void putActiveRequest(BrowserSwitchRequest request, Context context) {
        try {
            PersistentStore.put("browserSwitch", request.toJson(), context);
        } catch (JSONException ignored) { /* do nothing */ }
    }

    void clearActiveRequest(Context context) {
        PersistentStore.remove("browserSwitch", context);
    }
}
