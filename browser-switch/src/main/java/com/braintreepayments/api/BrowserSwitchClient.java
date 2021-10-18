package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

/**
 * Client that manages the logic for browser switching.
 */
public class BrowserSwitchClient {

    private final BrowserSwitchInspector browserSwitchInspector;
    private final BrowserSwitchPersistentStore persistentStore;

    private final ChromeCustomTabsInternalClient customTabsInternalClient;

    /**
     * Construct a client that manages the logic for browser switching.
     */
    public BrowserSwitchClient() {
        this(new BrowserSwitchInspector(), BrowserSwitchPersistentStore.getInstance(), new ChromeCustomTabsInternalClient());
    }

    @VisibleForTesting
    BrowserSwitchClient(BrowserSwitchInspector browserSwitchInspector, BrowserSwitchPersistentStore persistentStore, ChromeCustomTabsInternalClient customTabsInternalClient) {
        this.browserSwitchInspector = browserSwitchInspector;
        this.persistentStore = persistentStore;
        this.customTabsInternalClient = customTabsInternalClient;
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with a given set of {@link BrowserSwitchOptions} from an Android activity.
     *
     * @param activity the activity used to start browser switch
     * @param browserSwitchOptions {@link BrowserSwitchOptions} the options used to configure the browser switch
     */
    public void start(@NonNull FragmentActivity activity, @NonNull BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        assertCanPerformBrowserSwitch(activity, browserSwitchOptions);

        Context appContext = activity.getApplicationContext();

        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();

        JSONObject metadata = browserSwitchOptions.getMetadata();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(requestCode, browserSwitchUrl, metadata, returnUrlScheme, true);
        persistentStore.putActiveRequest(request, appContext);

        if (browserSwitchInspector.deviceHasChromeCustomTabs(appContext)) {
            customTabsInternalClient.launchUrl(activity, browserSwitchUrl);
        } else {
            Intent launchUrlInBrowser = new Intent(Intent.ACTION_VIEW, browserSwitchUrl);
            activity.startActivity(launchUrlInBrowser);
        }
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
        } else if (!browserSwitchInspector.deviceHasBrowser(appContext)) {
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
     * Deliver a pending browser switch result to an Android activity.
     *
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * Cancel and Success results will be delivered only once. If there are no pending
     * browser switch results, this method does nothing.
     *
     * @param activity the activity that received the deep link back into the app
     */
    public BrowserSwitchResult deliverResult(@NonNull FragmentActivity activity) {
        BrowserSwitchResult result = getResult(activity);
        if (result != null) {

            Context appContext = activity.getApplicationContext();
            BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);

            @BrowserSwitchStatus int status = result.getStatus();
            switch (status) {
                case BrowserSwitchStatus.SUCCESS:
                    // ensure that success result is delivered exactly once
                    persistentStore.clearActiveRequest(appContext);
                    break;
                case BrowserSwitchStatus.CANCELED:
                    // ensure that cancellation result is delivered exactly once, but allow for
                    // a cancellation result to remain in shared storage in case it
                    // later becomes successful
                    request.setShouldNotifyCancellation(false);
                    persistentStore.putActiveRequest(request, activity);
                    break;
            }
        }
        return result;
    }

    /**
     * Peek at a pending browser switch result to an Android activity.
     *
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * This can be used in place of {@link BrowserSwitchClient#deliverResult(FragmentActivity)} when
     * you want to know the contents of a pending browser switch result before it is delivered.
     *
     * @param activity the activity that received the deep link back into the app
     */
    BrowserSwitchResult getResult(@NonNull FragmentActivity activity) {
        Intent intent = activity.getIntent();
        Context appContext = activity.getApplicationContext();

        BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);
        if (request == null || intent == null) {
            // no pending browser switch request found
            return null;
        }

        BrowserSwitchResult result = null;

        Uri deepLinkUrl = intent.getData();
        if (deepLinkUrl != null && request.matchesDeepLinkUrlScheme(deepLinkUrl)) {
            result = new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, request, deepLinkUrl);
        } else if (request.getShouldNotifyCancellation()) {
            result = new BrowserSwitchResult(BrowserSwitchStatus.CANCELED, request);
        }

        return result;
    }
}
