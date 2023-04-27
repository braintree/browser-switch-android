package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.browserswitch.R;

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
     * @param activity             the activity used to start browser switch
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

        if (activity.isFinishing()) {
            String activityFinishingMessage =
                "Unable to start browser switch while host Activity is finishing.";
            throw new BrowserSwitchException(activityFinishingMessage);
        } else if (browserSwitchInspector.deviceHasChromeCustomTabs(appContext)) {
            boolean launchAsNewTask = browserSwitchOptions.isLaunchAsNewTask();
            customTabsInternalClient.launchUrl(activity, browserSwitchUrl, launchAsNewTask);
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
            errorMessage = activity.getString(R.string.error_request_code_invalid);
        } else if (returnUrlScheme == null) {
            errorMessage = activity.getString(R.string.error_return_url_required);
        } else if (!browserSwitchInspector.isDeviceConfiguredForDeepLinking(appContext, returnUrlScheme)) {
            errorMessage = activity.getString(R.string.error_device_not_configured_for_deep_link);
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
     * <p>
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     * <p>
     * Cancel and Success results will be delivered only once. If there are no pending
     * browser switch results, this method does nothing.
     *
     * @param activity the activity that received the deep link back into the app
     */
    public BrowserSwitchResult deliverResult(@NonNull FragmentActivity activity) {
        BrowserSwitchResult result = null;
        Context appContext = activity.getApplicationContext();
        BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);

        if (request != null) {
            result = getResult(activity);
            if (result != null) {
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
        }
        return result;
    }

    /**
     * Peek at a pending browser switch result to an Android activity.
     * <p>
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     * <p>
     * This can be used in place of {@link BrowserSwitchClient#deliverResult(FragmentActivity)} when
     * you want to know the contents of a pending browser switch result before it is delivered.
     *
     * @param activity the activity that received the deep link back into the app
     */
    public BrowserSwitchResult getResult(@NonNull FragmentActivity activity) {
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

    /**
     * Peek at a pending browser switch result that was previously captured by another Android activity.
     * <p>
     * This can be used in place of {@link #deliverResultFromCache(Context)} when
     * you want to know the contents of a cached browser switch result before it is delivered.
     *
     * @param context the context used to access the cache
     */
    public BrowserSwitchResult getResultFromCache(@NonNull Context context) {
        return persistentStore.getActiveResult(context.getApplicationContext());
    }

    /**
     * Deliver a pending browser switch result that was previously captured by another Android activity.
     * <p>
     * Success results will be delivered only once. If there are no pending
     * browser switch results in the cache, this method does nothing.
     *
     * @param context the context used to access the cache
     * @return {@link BrowserSwitchResult}
     */
    public BrowserSwitchResult deliverResultFromCache(@NonNull Context context) {
        BrowserSwitchResult result = getResultFromCache(context);
        if (result != null) {
            persistentStore.removeAll(context.getApplicationContext());
        }
        return result;
    }

    /**
     * Capture a pending browser switch result for an Android activity into a persistent storage cache.
     * <p>
     * To obtain the result in a separate activity, call {@link #deliverResultFromCache(Context)}.
     *
     * @param activity the activity that received the deep link back into the app
     */
    public void captureResult(@NonNull FragmentActivity activity) {
        Intent intent = activity.getIntent();
        Context appContext = activity.getApplicationContext();

        BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);
        if (request == null || intent == null) {
            // no pending browser switch request found
            return;
        }

        Uri deepLinkUrl = intent.getData();
        if (deepLinkUrl != null) {
            BrowserSwitchResult result =
                    new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, request, deepLinkUrl);
            persistentStore.putActiveResult(result, activity.getApplicationContext());
        }
    }
}
