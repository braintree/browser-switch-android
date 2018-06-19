package com.braintreepayments.browserswitch.test;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.braintreepayments.browserswitch.BrowserSwitchResult;
import com.braintreepayments.browserswitch.BrowserSwitchSupportFragment;

public class TestBrowserSwitchSupportFragment extends BrowserSwitchSupportFragment {

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
