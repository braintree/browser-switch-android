package com.braintreepayments.api;

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

    void launchUrl(Context context, Uri url) {
        CustomTabsIntent customTabsIntent = customTabsIntentBuilder.build();
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        customTabsIntent.launchUrl(context, url);
    }
}
