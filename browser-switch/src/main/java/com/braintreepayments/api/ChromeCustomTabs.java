package com.braintreepayments.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Helper class for working with Chrome Custom Tabs
 */
class ChromeCustomTabs {

    /**
     * Checks to see if this device supports Chrome Custom Tabs and if Chrome Custom Tabs are available.
     *
     * @param context Application context
     * @return {@code true} if Chrome Custom Tabs are supported and available.
     */
    static boolean isAvailable(Context context) {
        Intent serviceIntent = new Intent("android.support.customtabs.action.CustomTabsService")
                .setPackage("com.android.chrome");
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {}

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };

        boolean available = context.bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE | Context.BIND_WAIVE_PRIORITY);
        context.unbindService(connection);

        return available;
    }

    /**
     * Adds the required extras and flags to an {@link Intent} for using Chrome Custom Tabs. If
     * Chrome Custom Tabs are not available or supported no change will be made to the {@link Intent}.
     *
     * @param context Application context
     * @param intent The {@link Intent} to add the extras and flags to for Chrome Custom Tabs.
     */
    static void addChromeCustomTabsExtras(Context context, Intent intent) {
        if (ChromeCustomTabs.isAvailable(context)) {
            Bundle extras = new Bundle();
            extras.putBinder("android.support.customtabs.extra.SESSION", null);
            intent.putExtras(extras);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
    }
}
