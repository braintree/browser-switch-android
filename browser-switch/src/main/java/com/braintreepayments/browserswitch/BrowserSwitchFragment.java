package com.braintreepayments.browserswitch;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @deprecated {@link Fragment} is deprecated in Android P. Use {@link BrowserSwitchSupportFragment}
 * Abstract Fragment that manages the logic for browser switching.
 */
@Deprecated
public abstract class BrowserSwitchFragment extends Fragment {

    /**
     * Use {@link com.braintreepayments.browserswitch.BrowserSwitchResult}
     */
    @Deprecated
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

        static BrowserSwitchResult convert(@NonNull com.braintreepayments.browserswitch.BrowserSwitchResult result) {
            BrowserSwitchResult convertedResult = null;
            switch(result) {
                case OK:
                    convertedResult = BrowserSwitchResult.OK;
                    break;
                case ERROR:
                    convertedResult = BrowserSwitchResult.ERROR;
                    break;
                case CANCELED:
                    convertedResult = BrowserSwitchResult.CANCELED;
                    break;
            }

            convertedResult.setErrorMessage(result.getErrorMessage());

            return convertedResult;
        }
    }

    private static final String EXTRA_REQUEST_CODE = "com.braintreepayments.browserswitch.EXTRA_REQUEST_CODE";

    protected Context mContext;
    protected int mRequestCode;
    private BrowserSwitch mBrowserSwitch = new BrowserSwitch();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mContext == null) {
            mContext = getActivity().getApplicationContext();
        }

        if (savedInstanceState != null) {
            mRequestCode = savedInstanceState.getInt(EXTRA_REQUEST_CODE);
        } else {
            mRequestCode = Integer.MIN_VALUE;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isBrowserSwitching()) {
            Uri returnUri = BrowserSwitchActivity.getReturnUri();

            int requestCode = mRequestCode;
            mRequestCode = Integer.MIN_VALUE;
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
        outState.putInt(EXTRA_REQUEST_CODE, mRequestCode);
    }

    /**
     * @return the url scheme that can be used to return to the app from a web page. This url
     * scheme should be used to build a return url and passed to the target web page via a query
     * param when browser switching.
     */
    public String getReturnUrlScheme() {
        return mContext.getPackageName().toLowerCase().replace("_", "") + ".browserswitch";
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given url.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param url the url to open.
     */
    public void browserSwitch(int requestCode, String url) {
        Intent intent = mBrowserSwitch.getIntentFromUrl(mContext, url);
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
        com.braintreepayments.browserswitch.BrowserSwitchResult result =
                mBrowserSwitch.verifyBrowserSwitch(mContext, requestCode, intent);

        if (result != null) {
            onBrowserSwitchResult(requestCode, BrowserSwitchResult.convert(result), null);
            return;
        }

        mRequestCode = requestCode;
        mContext.startActivity(intent);
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
        return mRequestCode != Integer.MIN_VALUE;
    }
}