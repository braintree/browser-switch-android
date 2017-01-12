package com.braintreepayments.browserswitch.test;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.braintreepayments.browserswitch.BrowserSwitchFragment;

public class TestBrowserSwitchFragment extends BrowserSwitchFragment {

    public boolean onBrowserSwitchResultCalled = false;
    public int requestCode;
    public BrowserSwitchResult result;
    public Uri returnUri;

    @Override
    public void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result,
                                      @Nullable Uri returnUri) {
        onBrowserSwitchResultCalled = true;
        this.requestCode = requestCode;
        this.result = result;
        this.returnUri = returnUri;
    }
}
