package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class PublicBrowserSwitchLauncher {

    private BrowserSwitchLauncherCallback callback;
    private BrowserSwitchLauncher browserSwitchLauncher;
    private BrowserSwitchResult browserSwitchResult;

    public PublicBrowserSwitchLauncher(Fragment fragment, BrowserSwitchLauncherCallback callback) {
        this.callback = callback;
        this.browserSwitchLauncher = new BrowserSwitchLauncher(fragment, browserSwitchResult -> {
            this.browserSwitchResult = browserSwitchResult;
        });
    }

    public PublicBrowserSwitchLauncher(ComponentActivity activity, BrowserSwitchLauncherCallback callback) {
        this.callback = callback;
        this.browserSwitchLauncher = new BrowserSwitchLauncher(activity, browserSwitchResult -> {
            this.browserSwitchResult = browserSwitchResult;
        });
    }

    public void launch(BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        browserSwitchLauncher.launch(browserSwitchOptions);
    }

    public void handleReturnToAppFromBrowser(@NonNull Context context, int requestId, @NonNull Intent intent) {
        BrowserSwitchResult result = browserSwitchLauncher.parseResult(context, requestId, intent);
        if (result == null) {
            result = this.browserSwitchResult;
        }
        if (result != null) {
            callback.onResult(result);
            browserSwitchLauncher.clearActiveRequests(context);
            this.browserSwitchResult = null;
        }
    }
}
