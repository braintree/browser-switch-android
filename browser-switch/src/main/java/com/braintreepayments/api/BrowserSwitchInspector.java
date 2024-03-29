package com.braintreepayments.api;

import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

class BrowserSwitchInspector {

    boolean isDeviceConfiguredForDeepLinking(Context context, String returnUrlScheme) {
        String browserSwitchUrl = String.format("%s://", returnUrlScheme);
        Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserSwitchUrl));
        deepLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        deepLinkIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        return canResolveActivityForIntent(context, deepLinkIntent);
    }

    boolean deviceHasBrowser(Context context) {
        Intent browserSwitchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://"));
        return canResolveActivityForIntent(context, browserSwitchIntent);
    }

    boolean deviceHasChromeCustomTabs(Context context) {
        // Ref: https://developer.chrome.com/docs/android/custom-tabs/integration-guide/#how-can-i-check-whether-the-android-device-has-a-browser-that-supports-custom-tab
        PackageManager pm = context.getPackageManager();
        Intent activityIntent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("https://"));

        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        ArrayList<ResolveInfo> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);

            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info);
            }
        }
        return !packagesSupportingCustomTabs.isEmpty();
    }

    private boolean canResolveActivityForIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }
}
