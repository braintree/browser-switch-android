package com.braintreepayments.api;

import android.content.Intent;
import android.net.Uri;

class BrowserSwitchConfig {

    Intent createIntentToLaunchUriInBrowser(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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
