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

/**
 * Class that is used to deliver a browser switch result to a deep link destination FragmentActivity
 * and any attached Fragments that implement {@link BrowserSwitchListener}.
 */
public class BrowserSwitchObserver {

    private final BrowserSwitchPersistentStore persistentStore;
    private final BrowserSwitchListenerFinder listenerFinder;

    public BrowserSwitchObserver() {
        this(BrowserSwitchPersistentStore.getInstance(), new BrowserSwitchListenerFinder());
    }

    @VisibleForTesting
    BrowserSwitchObserver(BrowserSwitchPersistentStore persistentStore, BrowserSwitchListenerFinder listenerFinder) {
        this.persistentStore = persistentStore;
        this.listenerFinder = listenerFinder;
    }

    /**
     * Call this method in FragmentActivity#onResume to notify all listeners of a browser switch result.
     * @param activity Deep link destination activity
     */
    public void onActivityResumed(FragmentActivity activity) {
        BrowserSwitchResult result = getResult(activity);
        if (result != null) {

            Context appContext = activity.getApplicationContext();
            BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);

            List<BrowserSwitchListener> listeners = listenerFinder.findActiveListeners(activity);
            for (BrowserSwitchListener listener : listeners) {
                listener.onBrowserSwitchResult(result);
            }

            // TODO: consider if we should skip modifying persistent store when
            // there are no active listeners to deliver a result to
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

    /**
     * Peek at a pending browser switch result to an Android activity.
     * <p>
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     * <p>
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
