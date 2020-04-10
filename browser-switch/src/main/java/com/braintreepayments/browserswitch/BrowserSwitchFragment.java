package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Abstract Fragment that manages the logic for browser switching.
 */
public abstract class BrowserSwitchFragment extends Fragment implements BrowserSwitchListener {

    private final BrowserSwitch mBrowserSwitch;

    public void setContext(Context context) {
        mBrowserSwitch.setContext(context);
    }

    public Context getContext() {
        return mBrowserSwitch.getContext();
    }

    public void setRequestCode(int newRequestCode) {
        mBrowserSwitch.setRequestCode(newRequestCode);
    }

    public int getRequestCode() {
        return mBrowserSwitch.getRequestCode();
    }

    public BrowserSwitchFragment() {
        mBrowserSwitch = new BrowserSwitch(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() == null) {
            setContext(getActivity().getApplicationContext());
        }

        mBrowserSwitch.onCreate(new BundleBrowserSwitchData(savedInstanceState));
    }

    @Override
    public void onResume() {
        super.onResume();
        mBrowserSwitch.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mBrowserSwitch.onSaveInstanceState(new BundleBrowserSwitchData(outState));
    }

    /**
     * @return the url scheme that can be used to return to the app from a web page. This url
     * scheme should be used to build a return url and passed to the target web page via a query
     * param when browser switching.
     */
    public String getReturnUrlScheme() {
        return mBrowserSwitch.getReturnUrlScheme();
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given url.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param url         the url to open.
     */
    public void browserSwitch(int requestCode, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ChromeCustomTabs.addChromeCustomTabsExtras(getContext(), intent);

        browserSwitch(requestCode, intent);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given intent.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param intent      an {@link Intent} containing a url to open.
     */
    public void browserSwitch(int requestCode, Intent intent) {
        mBrowserSwitch.browserSwitch(requestCode, intent);
    }
}
