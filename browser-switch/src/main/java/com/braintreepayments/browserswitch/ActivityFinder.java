package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

class ActivityFinder {

    static ActivityFinder newInstance() {
        return new ActivityFinder();
    }

    private ActivityFinder() {}

    boolean canResolveActivityForIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }

    boolean deviceHasBrowser(Context context) {
        Intent browserSwitchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://"));
        return canResolveActivityForIntent(context, browserSwitchIntent);
    }
}
