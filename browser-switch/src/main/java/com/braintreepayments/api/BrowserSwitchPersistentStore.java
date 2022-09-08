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

    @VisibleForTesting
    static final String RESULT_KEY = "browserSwitch.result";

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
                // NEXT_MAJOR_VERSION: Add explicit error handling instead of ignoring exception
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
            // NEXT_MAJOR_VERSION: Add explicit error handling instead of ignoring exception
            Log.d(TAG, e.getMessage());
            Log.d(TAG, Arrays.toString(e.getStackTrace()));
        }
    }

    void putActiveResult(BrowserSwitchResult result, Context context) {
        try {
            PersistentStore.put(RESULT_KEY, result.toJson(), context);
        } catch (JSONException e) {
            // NEXT_MAJOR_VERSION: Add explicit error handling instead of ignoring exception
            Log.d(TAG, e.getMessage());
            Log.d(TAG, Arrays.toString(e.getStackTrace()));
        }
    }

    BrowserSwitchResult getActiveResult(Context context) {
        BrowserSwitchResult request = null;

        String activeResultJSON = PersistentStore.get(RESULT_KEY, context);
        if (activeResultJSON != null) {
            try {
                request = BrowserSwitchResult.fromJson(activeResultJSON);
            } catch (JSONException e) {
                // NEXT_MAJOR_VERSION: Add explicit error handling instead of ignoring exception
                Log.d(TAG, e.getMessage());
                Log.d(TAG, Arrays.toString(e.getStackTrace()));
            }
        }
        return request;
    }

    void clearActiveRequest(Context context) {
        PersistentStore.remove(REQUEST_KEY, context);
    }

    void removeAll(Context context) {
        PersistentStore.remove(RESULT_KEY, context);
        PersistentStore.remove(REQUEST_KEY, context);
    }
}
