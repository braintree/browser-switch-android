package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;

public class BrowserSwitch {

    private BrowserSwitch() {
        throw new AssertionError("BrowserSwitch should not be instantiated.");
    }

    @SuppressWarnings("WeakerAccess")
    public static BrowserSwitchEvent getResult() {
        // TODO: implement
        return null;
    }

    public static String getReturnUrlScheme(Context context) {
        // TODO: implement
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public static void start(int requestCode, Uri uri, Fragment fragment) {
        start(requestCode, uri, fragment, new Intent());
    }

    public static void start(int requestCode, Uri uri, FragmentActivity activity) {
        start(requestCode, uri, activity, new Intent());
    }

    @VisibleForTesting
    static void start(int requestCode, Uri uri, Fragment fragment, Intent intent) {
        BrowserSwitchListener listener = (BrowserSwitchListener) fragment;
        start(requestCode, uri, fragment.getActivity(), listener, intent);
    }

    @VisibleForTesting
    static void start(int requestCode, Uri uri, FragmentActivity activity, Intent intent) {
        BrowserSwitchListener listener = (BrowserSwitchListener) activity;
        start(requestCode, uri, activity, listener, intent);
    }

    private static void start(int requestCode, Uri uri, FragmentActivity activity, BrowserSwitchListener listener, Intent intent) {

        // write request code to db
        BrowserSwitchRepository repository = BrowserSwitchRepository.newInstance(activity.getApplication());
        repository.updatePendingRequest(requestCode, uri);

        PendingRequestObserver observer = PendingRequestObserver.newInstance(activity.getApplication(), listener);
        repository.getPendingRequest().observe(listener, observer);

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
