package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class BrowserSwitchLauncher {

    private BrowserSwitchLauncherCallback callback;
    private InternalBrowserSwitchLauncher internalBrowserSwitchLauncher;
    private BrowserSwitchResult browserSwitchResult;

    private Intent handledIntent;

    public BrowserSwitchLauncher(Fragment fragment, BrowserSwitchLauncherCallback callback) {
        this.callback = callback;
        this.internalBrowserSwitchLauncher = new InternalBrowserSwitchLauncher(fragment, browserSwitchResult -> {
            this.browserSwitchResult = browserSwitchResult;
        });
    }

    public BrowserSwitchLauncher(ComponentActivity activity, BrowserSwitchLauncherCallback callback) {
        this.callback = callback;
        this.internalBrowserSwitchLauncher = new InternalBrowserSwitchLauncher(activity, browserSwitchResult -> {
            // In the case of a true user cancellation (user closes the browser without completing),
            // this result will be delivered before handleReturnToAppFromBrowser is invoked in
            // onResume. After a successful result is delivered, the browser activity will be closed
            // in the background, delivering a false cancel result.
            this.browserSwitchResult = browserSwitchResult;
        });
    }

    public void launch(BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        internalBrowserSwitchLauncher.launch(browserSwitchOptions);
    }

    public void handleReturnToAppFromBrowser(@NonNull Context context, int requestId, @NonNull Intent intent) {
        BrowserSwitchResult result = null;
        // If browser-switch is re-launched from the same activity, a false previous success result
        // may be re-delivered even if the user cancels this time, check if we have already handled
        // the intent before delivering a success result again
        if (!intent.equals(handledIntent)) {
            // Handle successful deep link back to app first, to avoid false cancel result (above)
            result = internalBrowserSwitchLauncher.parseResult(context, requestId, intent);
        }
        if (result == null) {
            // If the app was not successfully resumed via deep link, check if there was a true
            // cancel result
            result = this.browserSwitchResult;
        }
        if (result != null) {
            callback.onResult(result);
            internalBrowserSwitchLauncher.clearActiveRequests(context);
            this.browserSwitchResult = null;
            handledIntent = intent;
        }
    }
}
