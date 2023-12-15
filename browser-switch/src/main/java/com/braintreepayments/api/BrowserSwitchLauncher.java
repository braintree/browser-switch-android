package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

public class BrowserSwitchLauncher {

    private static final String BROWSER_SWITCH_RESULT = "com.braintreepayments.api.BrowserSwitch.RESULT";

    @VisibleForTesting
    ActivityResultLauncher<BrowserSwitchOptions> activityLauncher;

    public BrowserSwitchLauncher(@NonNull ComponentActivity activity,
                             @NonNull BrowserSwitchLauncherCallback callback) {
        this(activity.getActivityResultRegistry(), activity, callback);
    }

    public BrowserSwitchLauncher(@NonNull Fragment fragment,
                                 @NonNull BrowserSwitchLauncherCallback callback) {
        this(fragment.getActivity().getActivityResultRegistry(), fragment.getViewLifecycleOwner(),
                callback);
    }

    @VisibleForTesting
    BrowserSwitchLauncher(ActivityResultRegistry registry, LifecycleOwner lifecycleOwner,
                      BrowserSwitchLauncherCallback callback) {
        activityLauncher = registry.register(BROWSER_SWITCH_RESULT, lifecycleOwner,
                new BrowserSwitchActivityResultContract(), callback::onResult);
    }

    public void launch(BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        try {
            activityLauncher.launch(browserSwitchOptions);
        } catch (ActivityNotFoundException e) {
            throw new BrowserSwitchException(e.getMessage());
        }
    }

    public void clearActiveRequests(@NonNull Context context) {
        BrowserSwitchPersistentStore.getInstance().clearActiveRequest(context.getApplicationContext());
    }

    @Nullable
    public BrowserSwitchResult parseResult(@NonNull Context context, int requestCode, @Nullable Intent intent) {
        BrowserSwitchResult result = null;
        if (intent != null && intent.getData() != null) {
            BrowserSwitchRequest request =
                    BrowserSwitchPersistentStore.getInstance().getActiveRequest(context.getApplicationContext());
            if (request != null && request.getRequestCode() == requestCode) {
                Uri deepLinkUrl = intent.getData();
                if (request.matchesDeepLinkUrlScheme(deepLinkUrl)) {
                    result = new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, request, deepLinkUrl);
                }
            }
        }
        return result;
    }
}
