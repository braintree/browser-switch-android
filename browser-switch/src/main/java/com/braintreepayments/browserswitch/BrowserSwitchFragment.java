package com.braintreepayments.browserswitch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * Abstract Fragment that manages the logic for browser switching.
 */
public abstract class BrowserSwitchFragment extends Fragment implements BrowserSwitchListener {

    @VisibleForTesting
    BrowserSwitchClient browserSwitchClient = null;

    private String returnUrlScheme;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        browserSwitchClient = BrowserSwitchClient.newInstance(getReturnUrlScheme());

        FragmentActivity activity = getActivity();
        if (activity != null) {
            String packageName = activity.getApplicationContext().getPackageName();
            returnUrlScheme =
                packageName.toLowerCase().replace("_", "") + ".browserswitch";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        browserSwitchClient.deliverResult(this);
    }

    /**
     * @return the url scheme that can be used to return to the app from a web page. This url
     * scheme should be used to build a return url and passed to the target web page via a query
     * param when browser switching.
     */
    @SuppressWarnings("WeakerAccess")
    public String getReturnUrlScheme() {
        return returnUrlScheme;
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given url.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param url the url to open.
     */
    @SuppressWarnings("WeakerAccess")
    public void browserSwitch(int requestCode, String url) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .requestCode(requestCode)
                .url(Uri.parse(url));
        browserSwitchClient.start(browserSwitchOptions, this);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given intent.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param intent an {@link Intent} containing a url to open.
     */
    @SuppressWarnings("WeakerAccess")
    public void browserSwitch(int requestCode, Intent intent) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .intent(intent)
                .requestCode(requestCode);
        browserSwitchClient.start(browserSwitchOptions, this);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given intent.
     *
     * @param browserSwitchOptions a {@link BrowserSwitchOptions} object.
     */
    public void browserSwitch(BrowserSwitchOptions browserSwitchOptions) {
        browserSwitchClient.start(browserSwitchOptions, this);
    }

    /**
     * The result of a browser switch will be returned in this method.
     *
     * @param requestCode the request code used to start this completed request.
     * @param result The state of the result, one of {@link BrowserSwitchResult#STATUS_OK},
     *     {@link BrowserSwitchResult#STATUS_CANCELED} or {@link BrowserSwitchResult#STATUS_ERROR}.
     * @param returnUri The return uri. {@code null} unless the result is {@link BrowserSwitchResult#STATUS_OK}.
     */
    public abstract void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri);
}
