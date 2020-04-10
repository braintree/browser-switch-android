package com.braintreepayments.browserswitch;

import android.net.Uri;

import androidx.annotation.Nullable;

public interface BrowserSwitchListener {
    /**
     * The result of a browser switch will be returned in this method.
     *
     * @param requestCode the request code used to start this completed request.
     * @param result The state of the result, one of {@link BrowserSwitchResult#STATUS_OK},
     *     {@link BrowserSwitchResult#STATUS_CANCELLED} or {@link BrowserSwitchResult#STATUS_ERROR}.
     * @param returnUri The return uri. {@code null} unless the result is {@link BrowserSwitchResult#STATUS_OK}.
     */
    void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri);
}
