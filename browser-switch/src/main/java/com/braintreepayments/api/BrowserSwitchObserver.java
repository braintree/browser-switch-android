package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public class BrowserSwitchObserver {

    private final BrowserSwitchPersistentStore persistentStore;

    public BrowserSwitchObserver() {
        this(BrowserSwitchPersistentStore.getInstance());
    }

    @VisibleForTesting
    BrowserSwitchObserver(BrowserSwitchPersistentStore persistentStore) {
        this.persistentStore = persistentStore;
    }

    public void onActivityResumed(FragmentActivity activity) {
        BrowserSwitchResult result = getResult(activity);
        if (result != null) {

            Context appContext = activity.getApplicationContext();
            BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);

            boolean wasDelivered = false;
            if (activity instanceof BrowserSwitchListener) {
                wasDelivered = true;
                ((BrowserSwitchListener) activity).onBrowserSwitchResult(result);
            }

            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof BrowserSwitchListener) {
                    wasDelivered = true;
                    ((BrowserSwitchListener) fragment).onBrowserSwitchResult(result);
                }
            }

            if (wasDelivered) {
                @BrowserSwitchStatus int status = result.getStatus();
                switch (status) {
                    case BrowserSwitchStatus.SUCCESS:
                        // ensure that success result is delivered exactly once
                        persistentStore.clearActiveRequest(appContext);
                        break;
                    case BrowserSwitchStatus.CANCELED:
                        // ensure that cancellation result is delivered exactly once, but allow for
                        // a cancellation result to remain in shared storage in case it
                        // later becomes successful
                        request.setShouldNotifyCancellation(false);
                        persistentStore.putActiveRequest(request, activity);
                        break;
                }
            }
        }
    }

    /**
     * Peek at a pending browser switch result to an Android activity.
     *
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * This can be used in place of {@link BrowserSwitchClient#deliverResult(FragmentActivity)} when
     * you want to know the contents of a pending browser switch result before it is delivered.
     *
     * @param activity the activity that received the deep link back into the app
     */
    BrowserSwitchResult getResult(@NonNull FragmentActivity activity) {
        Intent intent = activity.getIntent();
        Context appContext = activity.getApplicationContext();

        BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);
        if (request == null || intent == null) {
            // no pending browser switch request found
            return null;
        }

        BrowserSwitchResult result = null;

        Uri deepLinkUrl = intent.getData();
        if (deepLinkUrl != null && request.matchesDeepLinkUrlScheme(deepLinkUrl)) {
            result = new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, request, deepLinkUrl);
        } else if (request.getShouldNotifyCancellation()) {
            result = new BrowserSwitchResult(BrowserSwitchStatus.CANCELED, request);
        }

        return result;
    }
}
