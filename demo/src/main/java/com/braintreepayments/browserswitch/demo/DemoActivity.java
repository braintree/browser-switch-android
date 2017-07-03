package com.braintreepayments.browserswitch.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new DemoFragment())
                .commit();
    }
}
