package com.braintreepayments.api;

import android.net.Uri;

import androidx.annotation.Nullable;

public interface BrowserSwitchCallback {
    /**
     * The result of a browser switch will be returned in this method.
     *
     * @param requestCode the request code used to start this completed request.
     * @param result The state of the result, one of {@link BrowserSwitchResult#STATUS_OK} or
     *     {@link BrowserSwitchResult#STATUS_CANCELED}.
     * @param returnUri The return uri. {@code null} unless the result is {@link BrowserSwitchResult#STATUS_OK}.
     */
    void onResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri);
}
