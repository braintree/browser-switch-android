package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

class BrowserSwitchConfig {

    static BrowserSwitchConfig newInstance() {
        return new BrowserSwitchConfig();
    }

    Intent createIntentToLaunchUriInBrowser(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    Intent createIntentForBrowserSwitchActivityQuery(String returnUrlScheme) {
        String browserSwitchUrl = String.format("%s://", returnUrlScheme);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserSwitchUrl));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        return intent;
    }
}
