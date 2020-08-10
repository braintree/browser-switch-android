package com.braintreepayments.browserswitch;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsCallback;

public class ChromeCustomTabsCallback extends CustomTabsCallback {

    private static final String TAG = "BrowserCustomTabs";

    @Override
    public void onNavigationEvent(int navigationEvent, @Nullable Bundle extras) {
        super.onNavigationEvent(navigationEvent, extras);
        Log.d(TAG, getNavigationEventName(navigationEvent));
    }

    private static String getNavigationEventName(int navigationEvent) {
        switch (navigationEvent) {
            case CustomTabsCallback.NAVIGATION_STARTED:
                return "NAVIGATION_STARTED";
            case CustomTabsCallback.NAVIGATION_FINISHED:
                return "NAVIGATION_FINISHED";
            case CustomTabsCallback.NAVIGATION_FAILED:
                return "NAVIGATION_FAILED";
            case CustomTabsCallback.NAVIGATION_ABORTED:
                return "NAVIGATION_ABORTED";
            case CustomTabsCallback.TAB_SHOWN:
                return "TAB_SHOWN";
            case CustomTabsCallback.TAB_HIDDEN:
                return "TAB_HIDDEN";
            default:
                return "UNKNOWN";
        }
    }
}
