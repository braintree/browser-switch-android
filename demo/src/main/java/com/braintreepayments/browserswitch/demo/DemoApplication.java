package com.braintreepayments.browserswitch.demo;

import android.app.Application;
import android.content.Intent;

import com.braintreepayments.browserswitch.BrowserSwitchActivityDestroyedCallback;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BrowserSwitchActivityDestroyedCallback.register(
                this,
                new Intent(this, DemoActivity.class));
    }
}
