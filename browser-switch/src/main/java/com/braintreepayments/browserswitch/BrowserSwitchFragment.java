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
public abstract class BrowserSwitchFragment extends Fragment {

    public void setContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setRequestCode(int newRequestCode) {
        mRequestCode = newRequestCode;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public enum BrowserSwitchResult {
        OK,
        CANCELED,
        ERROR;

        private String mErrorMessage;

        public String getErrorMessage() {
            return mErrorMessage;
        }

        private BrowserSwitchResult setErrorMessage(String errorMessage) {
            mErrorMessage = errorMessage;
            return this;
        }

        @Override
        public String toString() {
            return name() + " " + getErrorMessage();
        }
    }

    private static final String EXTRA_REQUEST_CODE = "com.braintreepayments.browserswitch.EXTRA_REQUEST_CODE";

    private Context mContext;
    private int mRequestCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() == null) {
            setContext(getActivity().getApplicationContext());
        }

        if (savedInstanceState != null) {
            setRequestCode(savedInstanceState.getInt(EXTRA_REQUEST_CODE));
        } else {
            setRequestCode(Integer.MIN_VALUE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isBrowserSwitching()) {
            Uri returnUri = BrowserSwitchActivity.getReturnUri();

            int requestCode = getRequestCode();
            setRequestCode(Integer.MIN_VALUE);
            BrowserSwitchActivity.clearReturnUri();

            if (returnUri != null) {
                onBrowserSwitchResult(requestCode, BrowserSwitchResult.OK, returnUri);
            } else {
                onBrowserSwitchResult(requestCode, BrowserSwitchResult.CANCELED, null);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_REQUEST_CODE, getRequestCode());
    }

    /**
     * @return the url scheme that can be used to return to the app from a web page. This url
     * scheme should be used to build a return url and passed to the target web page via a query
     * param when browser switching.
     */
    public String getReturnUrlScheme() {
        return getContext().getPackageName().toLowerCase().replace("_", "") + ".browserswitch";
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given url.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param url the url to open.
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
     * @param intent an {@link Intent} containing a url to open.
     */
    public void browserSwitch(int requestCode, Intent intent) {
        if (requestCode == Integer.MIN_VALUE) {
            BrowserSwitchResult result = BrowserSwitchResult.ERROR
                    .setErrorMessage("Request code cannot be Integer.MIN_VALUE");
            onBrowserSwitchResult(requestCode, result, null);
            return;
        }

        if (!isReturnUrlSetup()) {
            BrowserSwitchResult result = BrowserSwitchResult.ERROR
                    .setErrorMessage("The return url scheme was not set up, incorrectly set up, " +
                            "or more than one Activity on this device defines the same url " +
                            "scheme in it's Android Manifest. See " +
                            "https://github.com/braintree/browser-switch-android for more " +
                            "information on setting up a return url scheme.");
            onBrowserSwitchResult(requestCode, result, null);
            return;
        } else if (availableActivities(intent).size() == 0) {
            BrowserSwitchResult result = BrowserSwitchResult.ERROR
                    .setErrorMessage(String.format("No installed activities can open this URL: %s", intent.getData().toString()));
            onBrowserSwitchResult(requestCode, result, null);
            return;
        }

        setRequestCode(requestCode);
        getContext().startActivity(intent);
    }

    /**
     * The result of a browser switch will be returned in this method.
     *
     * @param requestCode the request code used to start this completed request.
     * @param result The state of the result, one of {@link BrowserSwitchResult#OK},
     *     {@link BrowserSwitchResult#CANCELED} or {@link BrowserSwitchResult#ERROR}.
     * @param returnUri The return uri. {@code null} unless the result is {@link BrowserSwitchResult#OK}.
     */
    public abstract void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result,
                                               @Nullable Uri returnUri);

    private boolean isBrowserSwitching() {
        return getRequestCode() != Integer.MIN_VALUE;
    }

    private boolean isReturnUrlSetup() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(getReturnUrlScheme() + "://"))
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addCategory(Intent.CATEGORY_BROWSABLE);

        return availableActivities(intent).size() == 1;
    }

    private List<ResolveInfo> availableActivities(Intent intent) {
        return getContext().getPackageManager()
                .queryIntentActivities(intent, 0);
    }
}
