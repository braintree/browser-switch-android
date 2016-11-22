package com.braintreepayments.browser_switch;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

public abstract class BrowserSwitchFragment extends Fragment {

    public enum BrowserSwitchResult {
        OK,
        CANCELED
    }

    private static final String EXTRA_BROWSER_SWITCHING = "com.braintreepayments.browser_switch.EXTRA_BROWSER_SWITCHING";

    protected Context mContext;
    protected boolean mIsBrowserSwitching;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        if (savedInstanceState != null) {
            mIsBrowserSwitching = savedInstanceState.getBoolean(EXTRA_BROWSER_SWITCHING);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mIsBrowserSwitching) {
            Uri returnUri = BrowserSwitchActivity.getReturnUri();

            BrowserSwitchActivity.clearReturnUri();
            mIsBrowserSwitching = false;

            if (returnUri != null) {
                onBrowserSwitchResult(BrowserSwitchResult.OK, returnUri);
            } else {
                onBrowserSwitchResult(BrowserSwitchResult.CANCELED, null);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_BROWSER_SWITCHING, mIsBrowserSwitching);
    }

    public void browserSwitch(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (SDK_INT >= JELLY_BEAN_MR2 && isChromeCustomTabsAvailable()) {
            Bundle extras = new Bundle();
            extras.putBinder("android.support.customtabs.extra.SESSION", null);
            intent.putExtras(extras);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                    Intent.FLAG_ACTIVITY_NO_HISTORY);
        }

        browserSwitch(intent);
    }

    public void browserSwitch(Intent intent) {
        mIsBrowserSwitching = true;
        mContext.startActivity(intent);
    }

    public abstract void onBrowserSwitchResult(BrowserSwitchResult result, @Nullable Uri returnUri);

    private boolean isChromeCustomTabsAvailable() {
        Intent serviceIntent = new Intent("android.support.customtabs.action.CustomTabsService")
                .setPackage("com.android.chrome");
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {}

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };

        boolean chromeCustomTabsAvailable = mContext.bindService(serviceIntent, connection,
                Context.BIND_AUTO_CREATE | Context.BIND_WAIVE_PRIORITY);
        mContext.unbindService(connection);

        return chromeCustomTabsAvailable;
    }
}
