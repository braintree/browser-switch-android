package com.braintreepayments.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import org.json.JSONException;

import java.util.Arrays;

class BrowserSwitchPersistentStore {

    private static final String TAG = "BrowserSwitch";

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
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
                Log.d(TAG, Arrays.toString(e.getStackTrace()));
            }
        }
        return request;
    }

    void putActiveRequest(BrowserSwitchRequest request, Context context) {
        try {
            PersistentStore.put(REQUEST_KEY, request.toJson(), context);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            Log.d(TAG, Arrays.toString(e.getStackTrace()));
        }
    }

    void clearActiveRequest(Context context) {
        PersistentStore.remove(REQUEST_KEY, context);
    }
}
