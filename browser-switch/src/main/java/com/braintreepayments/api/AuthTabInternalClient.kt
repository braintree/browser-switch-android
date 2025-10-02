package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.VisibleForTesting;
import androidx.browser.auth.AuthTabIntent;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;

class AuthTabInternalClient {

    private final AuthTabIntent.Builder authTabIntentBuilder;
    private final CustomTabsIntent.Builder customTabsIntentBuilder;

    AuthTabInternalClient() {
        this(new AuthTabIntent.Builder(), new CustomTabsIntent.Builder());
    }

    @VisibleForTesting
    AuthTabInternalClient(AuthTabIntent.Builder authTabBuilder,
                          CustomTabsIntent.Builder customTabsBuilder) {
        this.authTabIntentBuilder = authTabBuilder;
        this.customTabsIntentBuilder = customTabsBuilder;
    }

    /**
     * Checks if Auth Tab is supported by the current browser
     */
    boolean isAuthTabSupported(Context context) {
        String packageName = CustomTabsClient.getPackageName(context, null);
        if (packageName == null) {
            return false;
        }
        return CustomTabsClient.isAuthTabSupported(context, packageName);
    }

    /**
     * Launch URL using Auth Tab if supported, otherwise fall back to Custom Tabs
     */
    void launchUrl(Context context,
                   Uri url,
                   String returnUrlScheme,
                   Uri appLinkUri,
                   ActivityResultLauncher<Intent> launcher,
                   LaunchType launchType) throws ActivityNotFoundException {

        if (launcher != null && isAuthTabSupported(context)) {
            // Auth Tab flow
            AuthTabIntent authTabIntent = authTabIntentBuilder.build();

            if (appLinkUri != null) {
                // For app links (HTTPS), extract host and path
                String host = appLinkUri.getHost();
                String path = appLinkUri.getPath();
                if (path == null) {
                    path = "/";
                }
                assert host != null;
                authTabIntent.launch(launcher, url, host, path);
            } else if (returnUrlScheme != null) {
                // For custom schemes
                authTabIntent.launch(launcher, url, returnUrlScheme);
            } else {
                throw new IllegalArgumentException("Either returnUrlScheme or appLinkUri must be provided");
            }
        } else {
            // Chrome Custom Tabs Fallback
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
}