package com.braintreepayments.api.browserswitch.demo;

import android.content.Intent;

public class DemoActivitySingleTop extends DemoActivity {

    private static final String RETURN_URL_SCHEME = "my-custom-url-scheme-standard";

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
    }

    @Override
    public String getReturnUrlScheme() {
        return RETURN_URL_SCHEME;
    }
}
