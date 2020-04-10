package com.braintreepayments.browserswitch;

import android.net.Uri;

import androidx.annotation.Nullable;

interface BrowserSwitchListener {

    /**
     * The result of a browser switch will be returned in this method.
     *
     * @param requestCode the request code used to start this completed request.
     * @param result The state of the result, one of {@link BrowserSwitchFragment.BrowserSwitchResult#OK},
     *     {@link BrowserSwitchFragment.BrowserSwitchResult#CANCELED} or {@link BrowserSwitchFragment.BrowserSwitchResult#ERROR}.
     * @param returnUri The return uri. {@code null} unless the result is {@link BrowserSwitchFragment.BrowserSwitchResult#OK}.
     */
    void onBrowserSwitchResult(int requestCode, BrowserSwitchFragment.BrowserSwitchResult result,
                                               @Nullable Uri returnUri);


}
