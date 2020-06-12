package com.braintreepayments.browserswitch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

// Ref: https://developer.chrome.com/multidevice/android/customtabs
public class BrowserCustomTabsClient extends CustomTabsServiceConnection {

    private static final String TAG = "BrowserCustomTabs";
    private static final String CHROME_PACKAGE_NAME = "com.android.chrome";

    private Uri targetUri;
    private Context context;

    private CustomTabsClient customTabsClient;
    private CustomTabsSession customTabsSession;

    public boolean startSession(Context context, Uri uri) {
        this.context = context;
        this.targetUri = uri;
        return CustomTabsClient.bindCustomTabsService(context, CHROME_PACKAGE_NAME, this);
    }

    @Override
    public void onCustomTabsServiceConnected(@NonNull ComponentName name, @NonNull CustomTabsClient client) {
        customTabsClient = client;
        customTabsClient.warmup(0L);

        Log.d(TAG, "Custom Tabs Service Connected");
        customTabsSession = customTabsClient.newSession(new BrowserCustomTabsCallback());

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(customTabsSession)
                .build();

        // Ref: https://stackoverflow.com/a/39974051
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        customTabsIntent.launchUrl(context, targetUri);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "Custom Tabs Service Disconnected");
    }
}
