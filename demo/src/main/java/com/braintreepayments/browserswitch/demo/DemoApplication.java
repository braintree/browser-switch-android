package com.braintreepayments.browserswitch.demo;

import android.app.Application;
import android.content.Intent;

import com.braintreepayments.browserswitch.BrowserSwitchActivity;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Edge case: In case our app process was killed when custom tabs was open,
        // let BrowserSwitchActivity know which activity to relaunch when it finishes.
        // Android will call its onCreate() with its original intent and extras,
        // then call its onNewIntent() with this intent without extras.
        BrowserSwitchActivity.setReturnIntent(
                new Intent(this, DemoActivity.class));
    }
}
