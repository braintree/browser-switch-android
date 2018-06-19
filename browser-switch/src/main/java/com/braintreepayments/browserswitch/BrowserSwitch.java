package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;

class BrowserSwitch {
    Intent getIntentFromUrl(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ChromeCustomTabs.addChromeCustomTabsExtras(context, intent);

        return intent;
    }

    BrowserSwitchResult verifyBrowserSwitch(Context context, int requestCode, Intent intent) {
        if (requestCode == Integer.MIN_VALUE) {
            BrowserSwitchResult result = BrowserSwitchResult.ERROR
                    .setErrorMessage("Request code cannot be Integer.MIN_VALUE");
            return result;
        }

        if (!isReturnUrlSetup(context)) {
            BrowserSwitchResult result = BrowserSwitchResult.ERROR
                    .setErrorMessage("The return url scheme was not set up, incorrectly set up, " +
                            "or more than one Activity on this device defines the same url " +
                            "scheme in it's Android Manifest. See " +
                            "https://github.com/braintree/browser-switch-android for more " +
                            "information on setting up a return url scheme.");
            return result;
        } else if (availableActivities(context.getPackageManager(), intent).size() == 0) {
            BrowserSwitchResult result = BrowserSwitchResult.ERROR
                    .setErrorMessage(String.format("No installed activities can open this URL: %s", intent.getData().toString()));
            return result;
        }

        return null;
    }

    public String getReturnUrlScheme(Context context) {
        return context.getPackageName().toLowerCase().replace("_", "") + ".browserswitch";
    }

    private boolean isReturnUrlSetup(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(getReturnUrlScheme(context) + "://"))
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addCategory(Intent.CATEGORY_BROWSABLE);

        return availableActivities(context.getPackageManager(), intent).size() == 1;
    }

    private List<ResolveInfo> availableActivities(PackageManager pm, Intent intent) {
        return pm.queryIntentActivities(intent, 0);
    }
}
