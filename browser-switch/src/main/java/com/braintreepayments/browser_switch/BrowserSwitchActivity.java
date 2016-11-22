package com.braintreepayments.browser_switch;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class BrowserSwitchActivity extends Activity {

    private static Uri sReturnUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sReturnUri = null;
        if (getIntent() != null && getIntent().getData() != null) {
            sReturnUri = getIntent().getData();
        }

        finish();
    }

    public static Uri getReturnUri() {
        return sReturnUri;
    }

    public static void clearReturnUri() {
        sReturnUri = null;
    }
}
