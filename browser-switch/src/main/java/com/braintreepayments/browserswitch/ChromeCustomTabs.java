package com.braintreepayments.browserswitch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * Helper class for working with Chrome Custom Tabs
 */
public class ChromeCustomTabs {

    /**
     * Checks to see if this device supports Chrome Custom Tabs and if Chrome Custom Tabs are available.
     *
     * @param context Application context
     * @return {@code true} if Chrome Custom Tabs are supported and available.
     */
    public static boolean isAvailable(Context context) {

        CustomTabsServiceConnection connection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(@NonNull ComponentName name, @NonNull CustomTabsClient client) {}

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };

        boolean available = CustomTabsClient.bindCustomTabsService(context, "com.android.chrome", connection);
        context.unbindService(connection);

        return available;
    }

    static void launchUrl(Context context, Uri url) {
        // TODO: unit test
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, url);
    }

    /**
     * Adds the required extras and flags to an {@link Intent} for using Chrome Custom Tabs. If
     * Chrome Custom Tabs are not available or supported no change will be made to the {@link Intent}.
     *
     * @param context Application context
     * @param intent The {@link Intent} to add the extras and flags to for Chrome Custom Tabs.
     * @return The {@link Intent} supplied with additional extras and flags if Chrome Custom Tabs
     *         are supported and available.
     */
    public static Intent addChromeCustomTabsExtras(Context context, Intent intent) {
        if (ChromeCustomTabs.isAvailable(context)) {
            Bundle extras = new Bundle();
            extras.putBinder("android.support.customtabs.extra.SESSION", null);
            intent.putExtras(extras);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        return intent;
    }
}
