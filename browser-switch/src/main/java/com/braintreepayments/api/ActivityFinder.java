package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;

class ActivityFinder {

    ActivityFinder() {}

    boolean canResolveActivityForIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }
}
