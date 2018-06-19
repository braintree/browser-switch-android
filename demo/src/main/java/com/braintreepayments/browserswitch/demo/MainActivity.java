package com.braintreepayments.browserswitch.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startBrowserSwitchFragment(View view) {
        Intent demoActivity = new Intent(this, DemoActivity.class);
        startActivity(demoActivity);
    }

    public void startBrowserSwitchSupportFragment(View view) {
        Intent demoSupportActivity = new Intent(this, DemoSupportActivity.class);
        startActivity(demoSupportActivity);
    }
}
