package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;

class ActivityFinder {

    static ActivityFinder newInstance() {
        return new ActivityFinder();
    }

    private ActivityFinder() {}

    boolean canResolveActivityForIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }
}
