package com.braintreepayments.browserswitch.test;

import android.net.Uri;

import com.braintreepayments.browserswitch.BrowserSwitchEvent;
import com.braintreepayments.browserswitch.BrowserSwitchFragment;
import com.braintreepayments.browserswitch.BrowserSwitchResult;

import androidx.annotation.Nullable;

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

    @Override
    public void onBrowserSwitchEvent(BrowserSwitchEvent event) {
        // TODO: remove when fragment code removed
    }
}
