package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

public class BrowserSwitch {

    private BrowserSwitch() {
        throw new AssertionError("BrowserSwitch should not be instantiated.");
    };

    @SuppressWarnings("WeakerAccess")
    public static void start(int requestCode, Uri uri, AppCompatActivity activity) {
        start(requestCode, uri, activity, new Intent());
    }

    @VisibleForTesting
    static void start(int requestCode, Uri uri, AppCompatActivity activity, Intent intent) {

        // write request code to db
        BrowserSwitchRepository repository =
            BrowserSwitchRepository.newInstance(activity.getApplication());
        // TODO: consider renaming to insertAsync
        repository.insert(new PendingRequest(requestCode, 0));

        intent.setData(uri);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Context applicationContext = activity.getApplicationContext();
        if (ChromeCustomTabs.isAvailable(applicationContext)) {
            ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
        }

        applicationContext.startActivity(intent);
    }
}
