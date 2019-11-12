package com.braintreepayments.browserswitch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * Helper class for working with Chrome Custom Tabs
 */
public class ChromeCustomTabs {

    // Action to add to the service intent. This action can be used as a way
    // generically pick apps that handle custom tabs for both activity and service
    // side implementations.
    private static final String ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService";
    /**
     * Checks to see if this device supports Chrome Custom Tabs and if Chrome Custom Tabs are available.
     *
     * @param context
     * @return {@code true} if Chrome Custom Tabs are supported and available.
     */
    public static boolean isAvailable(Context context) {
        if (SDK_INT < JELLY_BEAN_MR2) {
            return false;
        }
        // https://developer.chrome.com/multidevice/android/customtabs
        PackageManager pm = context.getPackageManager();
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            // Check if this package also resolves the Custom Tabs service.
            if (pm.resolveService(serviceIntent, 0) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the required extras and flags to an {@link Intent} for using Chrome Custom Tabs. If
     * Chrome Custom Tabs are not available or supported no change will be made to the {@link Intent}.
     *
     * @param context
     * @param intent The {@link Intent} to add the extras and flags to for Chrome Custom Tabs.
     * @return The {@link Intent} supplied with additional extras and flags if Chrome Custom Tabs
     *         are supported and available.
     */
    public static Intent addChromeCustomTabsExtras(Context context, Intent intent) {
        if (SDK_INT >= JELLY_BEAN_MR2 && ChromeCustomTabs.isAvailable(context)) {
            Bundle extras = new Bundle();
            extras.putBinder("android.support.customtabs.extra.SESSION", null);
            intent.putExtras(extras);
        }

        return intent;
    }
}
