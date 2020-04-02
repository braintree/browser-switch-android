package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;

public class BrowserSwitch {

    private BrowserSwitch() {
        throw new AssertionError("BrowserSwitch should not be instantiated.");
    };

    @SuppressWarnings("WeakerAccess")
    public static void start(int requestCode, Uri uri, Context context) {
        start(requestCode, uri, context, new Intent());
    }

    @VisibleForTesting
    static void start(int requestCode, Uri uri, Context context, Intent intent) {
        intent.setData(uri);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Context applicationContext = context.getApplicationContext();
        if (ChromeCustomTabs.isAvailable(applicationContext)) {
            ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
        }

        applicationContext.startActivity(intent);
    }
}
