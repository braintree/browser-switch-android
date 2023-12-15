package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LifecycleOwner;

public class BrowserSwitchLauncher {

    private static final String BROWSER_SWITCH_RESULT = "com.braintreepayments.api.BrowserSwitch.RESULT";

    @VisibleForTesting
    ActivityResultLauncher<BrowserSwitchOptions> activityLauncher;

    public BrowserSwitchLauncher(@NonNull ComponentActivity activity,
                             @NonNull BrowserSwitchLauncherCallback callback) {
        this(activity.getActivityResultRegistry(), activity, callback);
    }

    @VisibleForTesting
    BrowserSwitchLauncher(ActivityResultRegistry registry, LifecycleOwner lifecycleOwner,
                      BrowserSwitchLauncherCallback callback) {
        activityLauncher = registry.register(BROWSER_SWITCH_RESULT, lifecycleOwner,
                new BrowserSwitchActivityResultContract(), callback::onResult);
    }

    public void launch(BrowserSwitchOptions browserSwitchOptions) {
        activityLauncher.launch(browserSwitchOptions);
    }

    public void clearActiveRequests(@NonNull Context context) {
        BrowserSwitchPersistentStore.getInstance().clearActiveRequest(context.getApplicationContext());
    }
}
