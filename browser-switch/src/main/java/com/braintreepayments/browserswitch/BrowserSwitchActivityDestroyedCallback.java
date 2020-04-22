package com.braintreepayments.browserswitch;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Launches the specified activity when BrowserSwitchActivity is destroyed.
 */
public class BrowserSwitchActivityDestroyedCallback implements Application.ActivityLifecycleCallbacks {

    @Nullable
    private static BrowserSwitchActivityDestroyedCallback sInstance = null;

    @NonNull
    private final Intent initialActivityIntent;

    private BrowserSwitchActivityDestroyedCallback(@NonNull Intent initialActivityIntent) {
        this.initialActivityIntent = initialActivityIntent;
    }

    /**
     * @param intent the intent of the activity to launch when the BrowserSwitchActivity is destroyed
     */
    @MainThread
    static void register(@NonNull Application application, @NonNull Intent intent) {
        unregister(application);
        sInstance = new BrowserSwitchActivityDestroyedCallback(intent);
        application.registerActivityLifecycleCallbacks(sInstance);
    }

    @MainThread
    static void unregister(@NonNull Application application) {
        if (sInstance != null) {
            application.unregisterActivityLifecycleCallbacks(sInstance);
            sInstance = null;
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        // https://stackoverflow.com/questions/39726547/i-want-to-close-chrome-custom-tab-when-action-button-is-clicked
        // Relaunch ourselves, bringing ourselves to the front, when the BrowserSwitchActivity
        // self-finishes.
        if (activity instanceof BrowserSwitchActivity) {
            Intent relaunchActivityIntent = new Intent(initialActivityIntent)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(relaunchActivityIntent);
        }
    }
}

