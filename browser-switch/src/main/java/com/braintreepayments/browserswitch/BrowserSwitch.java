package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;

class BrowserSwitch {
    private final BrowserSwitchListener mListener;
    private Context mContext;
    private int mRequestCode;

    public BrowserSwitch(BrowserSwitchListener listener) {
        mListener = listener;
    }

    Context getContext() {
        return mContext;
    }

    void setContext(Context context) {
        mContext = context;
    }

    int getRequestCode() {
        return mRequestCode;
    }

    void setRequestCode(int newRequestCode) {
        mRequestCode = newRequestCode;
    }

    void onCreate(BrowserSwitchData data) {
        setRequestCode(data.getRequestCode());
    }

    void onResume() {
        if (isBrowserSwitching()) {
            Uri returnUri = BrowserSwitchActivity.getReturnUri();

            int requestCode = getRequestCode();
            setRequestCode(Integer.MIN_VALUE);
            BrowserSwitchActivity.clearReturnUri();

            if (returnUri != null) {
                mListener.onBrowserSwitchResult(requestCode, BrowserSwitchResult.ok(), returnUri);
            } else {
                mListener.onBrowserSwitchResult(requestCode, BrowserSwitchResult.cancelled(), null);
            }
        }
    }

    void onSaveInstanceState(BrowserSwitchData data) {
        data.setRequestCode(getRequestCode());
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
            BrowserSwitchResult result = BrowserSwitchResult.error("Request code cannot be Integer.MIN_VALUE");
            mListener.onBrowserSwitchResult(requestCode, result, null);
            return;
        }

        if (!isReturnUrlSetup()) {
            BrowserSwitchResult result = BrowserSwitchResult.error(
                    "The return url scheme was not set up, incorrectly set up, " +
                            "or more than one Activity on this device defines the same url " +
                            "scheme in it's Android Manifest. See " +
                            "https://github.com/braintree/browser-switch-android for more " +
                            "information on setting up a return url scheme.");
            mListener.onBrowserSwitchResult(requestCode, result, null);
            return;
        } else if (availableActivities(intent).size() == 0) {
            BrowserSwitchResult result = BrowserSwitchResult.error(
                    String.format("No installed activities can open this URL: %s", intent.getData().toString()));
            mListener.onBrowserSwitchResult(requestCode, result, null);
            return;
        }

        setRequestCode(requestCode);
        getContext().startActivity(intent);
    }

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

    /**
     * @return the url scheme that can be used to return to the app from a web page. This url
     * scheme should be used to build a return url and passed to the target web page via a query
     * param when browser switching.
     */
    String getReturnUrlScheme() {
        return getContext().getPackageName().toLowerCase().replace("_", "") + ".browserswitch";
    }
}
