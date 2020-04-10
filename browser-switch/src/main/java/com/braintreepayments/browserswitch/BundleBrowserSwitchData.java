package com.braintreepayments.browserswitch;

import android.os.Bundle;

import java.util.Optional;

class BundleBrowserSwitchData implements BrowserSwitchData {
    private static final String EXTRA_REQUEST_CODE = "com.braintreepayments.browserswitch.EXTRA_REQUEST_CODE";

    private final Optional<Bundle> mSavedInstanceState;

    public BundleBrowserSwitchData(Bundle savedInstanceState) {
        mSavedInstanceState = Optional.ofNullable(savedInstanceState);
    }

    @Override
    public int getRequestCode() {
        return mSavedInstanceState.map(b -> b.getInt(EXTRA_REQUEST_CODE)).orElse(Integer.MIN_VALUE);
    }

    @Override
    public void setRequestCode(int requestCode) {
        mSavedInstanceState.ifPresent(b -> b.putInt(EXTRA_REQUEST_CODE, requestCode));
    }
}
