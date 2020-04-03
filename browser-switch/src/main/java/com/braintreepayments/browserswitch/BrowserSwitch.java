package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

public class BrowserSwitch {

    private BrowserSwitch() {
        throw new AssertionError("BrowserSwitch should not be instantiated.");
    };

    @SuppressWarnings("WeakerAccess")
    public static void start(int requestCode, Uri uri, Fragment fragment) {
        start(requestCode, uri, fragment.getActivity(), fragment, new Intent());
    }

    @SuppressWarnings("WeakerAccess")
    public static void start(int requestCode, Uri uri, FragmentActivity activity) {
        start(requestCode, uri, activity, activity, new Intent());
    }

    @VisibleForTesting
    static void start(int requestCode, Uri uri, FragmentActivity activity, LifecycleOwner lifecycleOwner, Intent intent) {

        // write request code to db
        BrowserSwitchRepository repository =
            BrowserSwitchRepository.newInstance(activity.getApplication());
        // TODO: consider renaming to insertAsync
        repository.insert(new PendingRequest(BrowserSwitchConstants.PENDING_REQUEST_ID, requestCode, 0));

        // TODO: ensure no memory leaks occur here (i.e. make sure we aren't responsible for removing the observer)
        repository.getPendingRequest().observe(lifecycleOwner, pendingRequest -> {
            if (pendingRequest != null) {
                if (lifecycleOwner instanceof BrowserSwitchListener) {
                    // TODO: notify error if activity is not a browser switch listener
                    BrowserSwitchFragment.BrowserSwitchResult result = BrowserSwitchFragment.BrowserSwitchResult.OK;
                    ((BrowserSwitchListener) lifecycleOwner).onBrowserSwitchEvent(new BrowserSwitchEvent(result, requestCode, uri));
                }

                // delete all pending events now that we've notified completion; this is backwards
                // compatible with previous versions of browser switch
                repository.deleteAll();
                repository.getPendingRequest().removeObservers(lifecycleOwner);
            }
        });

        intent.setData(uri);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Context applicationContext = activity.getApplicationContext();
        if (ChromeCustomTabs.isAvailable(applicationContext)) {
            ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
        }

        applicationContext.startActivity(intent);
    }
}
