package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

public class BrowserSwitchLauncher {

    private final BrowserSwitchInspector browserSwitchInspector;
    private final BrowserSwitchPersistentStore persistentStore;

    private final ChromeCustomTabsInternalClient customTabsInternalClient;

    /**
     * Construct a launcher to start a browser switch.
     */
    public BrowserSwitchLauncher() {
        this(new BrowserSwitchInspector(), BrowserSwitchPersistentStore.getInstance(), new ChromeCustomTabsInternalClient());
    }

    @VisibleForTesting
    BrowserSwitchLauncher(BrowserSwitchInspector browserSwitchInspector, BrowserSwitchPersistentStore persistentStore, ChromeCustomTabsInternalClient customTabsInternalClient) {
        this.browserSwitchInspector = browserSwitchInspector;
        this.persistentStore = persistentStore;
        this.customTabsInternalClient = customTabsInternalClient;
    }

    public void launch(@NonNull FragmentActivity activity, @NonNull BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException  {
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
}
