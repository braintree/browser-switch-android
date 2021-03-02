package com.braintreepayments.api;

import android.content.Context;
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
        customTabsIntent.launchUrl(context, url);
    }
}
