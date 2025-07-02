package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;
import androidx.browser.customtabs.CustomTabsIntent;

class ChromeCustomTabsInternalClient {

    private final CustomTabsIntent.Builder customTabsIntentBuilder;

    ChromeCustomTabsInternalClient() {
        this(new CustomTabsIntent.Builder());
    }

    @VisibleForTesting
    ChromeCustomTabsInternalClient(CustomTabsIntent.Builder builder) {
        this.customTabsIntentBuilder = builder;
    }

    void launchUrl(Context context, Uri url, LaunchType launchType) throws ActivityNotFoundException {
        CustomTabsIntent customTabsIntent = customTabsIntentBuilder.build();
        if (launchType != null) {
            switch (launchType) {
                case ACTIVITY_NEW_TASK:
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTIVITY_CLEAR_TOP:
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    break;
            }
        }
        customTabsIntent.launchUrl(context, url);
    }
}
