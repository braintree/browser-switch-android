package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;

public class BrowserSwitch {

    private int mRequestCode;
    private Context mContext;

    public void setRequestCode(int newCode) {
        mRequestCode = newCode;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public static String getReturnUrlScheme() {
        return BuildConfig.APPLICATION_ID + ".browserswitch";
    }

    @SuppressWarnings("WeakerAccess")
    public static void start(int requestCode, Uri uri, Fragment fragment) {
        start(requestCode, uri, fragment, new Intent());
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

    // TODO: Do we want context here? Can we delete it?
    public void setContext(Context newContext) {
        mContext = newContext;
    }

    public Context getContext() {
        return mContext;
    }

    public void onCreate(FragmentActivity activity, int requestCode) {
        if (getContext() == null) {
            setContext(activity.getApplicationContext());
        }

        setRequestCode(requestCode);
    }

    public void onResume() {
        if (isBrowserSwitching()) {
            Uri returnUri = BrowserSwitchActivity.getReturnUri();

            int requestCode = getRequestCode();
//            mRequestCode = Integer.MIN_VALUE;
            BrowserSwitchActivity.clearReturnUri();

            if (returnUri != null) {
                onBrowserSwitchResult(requestCode, BrowserSwitchResult.OK, returnUri);
            } else {
                onBrowserSwitchResult(requestCode, BrowserSwitchResult.CANCELED, null);
            }
        }
    }
}
