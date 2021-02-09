package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

class BrowserSwitchInspector {

    boolean isDeviceConfiguredForDeepLinking(Context context, String returnUrlScheme) {
        String browserSwitchUrl = String.format("%s://", returnUrlScheme);
        Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserSwitchUrl));
        deepLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        deepLinkIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        return canResolveActivityForIntent(context, deepLinkIntent);
    }

    boolean canDeviceOpenUrl(Context context, Uri uri) {
        Intent browserSwitchIntent = new Intent(Intent.ACTION_VIEW, uri);
        return canResolveActivityForIntent(context, browserSwitchIntent);
    }

    private boolean canResolveActivityForIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }
}
