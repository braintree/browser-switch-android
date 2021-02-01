package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

public class BrowserSwitchClient {

    final private BrowserSwitchInspector browserSwitchInspector;
    final private BrowserSwitchPersistentStore persistentStore;

    final private CustomTabsIntent.Builder customTabsIntentBuilder;

    public BrowserSwitchClient() {
        this(new BrowserSwitchInspector(), BrowserSwitchPersistentStore.getInstance(), new CustomTabsIntent.Builder());
    }

    @VisibleForTesting
    BrowserSwitchClient(BrowserSwitchInspector browserSwitchInspector, BrowserSwitchPersistentStore persistentStore, CustomTabsIntent.Builder customTabsIntentBuilder) {
        this.browserSwitchInspector = browserSwitchInspector;
        this.persistentStore = persistentStore;
        this.customTabsIntentBuilder = customTabsIntentBuilder;
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with a given set of {@link BrowserSwitchOptions} from an Android activity.
     *
     * @param browserSwitchOptions {@link BrowserSwitchOptions}
     * @param activity the activity used to start browser switch
     */
    public void start(FragmentActivity activity, BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        assertCanPerformBrowserSwitch(activity, browserSwitchOptions);

        Context appContext = activity.getApplicationContext();

        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();

        JSONObject metadata = browserSwitchOptions.getMetadata();
        BrowserSwitchRequest request = new BrowserSwitchRequest(
                requestCode, browserSwitchUrl, metadata);
        persistentStore.putActiveRequest(request, appContext);

        CustomTabsIntent customTabsIntent = customTabsIntentBuilder.build();
        customTabsIntent.launchUrl(activity, browserSwitchUrl);
    }

    void assertCanPerformBrowserSwitch(FragmentActivity activity, BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        Context appContext = activity.getApplicationContext();

        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();

        String errorMessage = null;

        if (!isValidRequestCode(requestCode)) {
            errorMessage = "Request code cannot be Integer.MIN_VALUE";
        } else if (returnUrlScheme == null) {
            errorMessage = "A returnUrlScheme is required.";
        } else if (!browserSwitchInspector.isDeviceConfiguredForDeepLinking(appContext, returnUrlScheme)) {
            errorMessage =
                "The return url scheme was not set up, incorrectly set up, " +
                "or more than one Activity on this device defines the same url " +
                "scheme in it's Android Manifest. See " +
                "https://github.com/braintree/browser-switch-android for more " +
                "information on setting up a return url scheme.";
        } else if (!browserSwitchInspector.canDeviceOpenUrl(appContext, browserSwitchUrl)) {
            StringBuilder messageBuilder = new StringBuilder("No installed activities can open this URL");
            if (browserSwitchUrl != null) {
                messageBuilder.append(String.format(": %s", browserSwitchUrl.toString()));
            }
            errorMessage = messageBuilder.toString();
        }

        if (errorMessage != null) {
            throw new BrowserSwitchException(errorMessage);
        }
    }

    private boolean isValidRequestCode(int requestCode) {
        return requestCode != Integer.MIN_VALUE;
    }

    /**
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * Cancel and Success results will be delivered only once. If there are no pending
     * browser switch results, this method does nothing.
     *
     * @param activity the activity that received the deep link back into the app
     */
    public BrowserSwitchResult deliverResult(@NonNull FragmentActivity activity) {
        Intent intent = activity.getIntent();
        Context appContext = activity.getApplicationContext();

        BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);
        if (request == null || intent == null) {
            // no pending browser switch request found
            return null;
        }

        BrowserSwitchResult result;

        Uri deepLinkUri = intent.getData();
        if (deepLinkUri == null) {
            result = new BrowserSwitchResult(BrowserSwitchResult.STATUS_CANCELED, request);
        } else {
            result = new BrowserSwitchResult(BrowserSwitchResult.STATUS_SUCCESS, request, deepLinkUri);
        }

        // ensure that browser switch result is delivered exactly one time
        persistentStore.clearActiveRequest(appContext);
        return result;
    }
}
