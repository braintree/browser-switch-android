package com.braintreepayments.api.demo;

import android.content.Intent;

public class DemoActivitySingleTop extends DemoActivity {

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
    }
}
