package com.braintreepayments.browser_switch.test;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.braintreepayments.browser_switch.BrowserSwitchFragment;

public class TestBrowserSwitchFragment extends BrowserSwitchFragment {

    public boolean onBrowserSwitchResultCalled = false;
    public BrowserSwitchResult result;
    public Uri returnUri;

    @Override
    public void onBrowserSwitchResult(BrowserSwitchResult result, @Nullable Uri returnUri) {
        onBrowserSwitchResultCalled = true;
        this.result = result;
        this.returnUri = returnUri;
    }
}
