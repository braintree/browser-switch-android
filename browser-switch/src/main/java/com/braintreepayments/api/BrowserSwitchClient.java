package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.browserswitch.R;

import org.json.JSONObject;

/**
 * Client that manages the logic for browser switching.
 */
// NEXT_MAJOR_VERSION remove all methods except start with ComponentActivity and parseResult with BrowserSwitchRequest
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
     * @return a {@link BrowserSwitchPendingRequest.Started} that should be stored and passed to
     * {@link BrowserSwitchClient#parseResult(BrowserSwitchPendingRequest.Started, Intent)} upon return to the app,
     * or {@link BrowserSwitchPendingRequest.Failure} if browser could not be launched.
     */
    @NonNull
    public BrowserSwitchPendingRequest start(@NonNull ComponentActivity activity, @NonNull BrowserSwitchOptions browserSwitchOptions) {
        try {
            assertCanPerformBrowserSwitch(activity, browserSwitchOptions);
        } catch (BrowserSwitchException e) {
            return new BrowserSwitchPendingRequest.Failure(e);
        }

        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();

        JSONObject metadata = browserSwitchOptions.getMetadata();

        if (activity.isFinishing()) {
            String activityFinishingMessage =
                    "Unable to start browser switch while host Activity is finishing.";
            return new BrowserSwitchPendingRequest.Failure(new BrowserSwitchException(activityFinishingMessage));
        } else  {
            boolean launchAsNewTask = browserSwitchOptions.isLaunchAsNewTask();
            BrowserSwitchRequest request;
            try {
                 request =
                        new BrowserSwitchRequest(requestCode, browserSwitchUrl, metadata, returnUrlScheme, true);
                customTabsInternalClient.launchUrl(activity, browserSwitchUrl, launchAsNewTask);
            } catch (ActivityNotFoundException e) {
                return new BrowserSwitchPendingRequest.Failure(new BrowserSwitchException("Unable to start browser switch without a web browser."));
            }
            return new BrowserSwitchPendingRequest.Started(request);
        }
    }

    void assertCanPerformBrowserSwitch(ComponentActivity activity, BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        Context appContext = activity.getApplicationContext();

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
     * Parses and returns a browser switch result if a match is found.
     *
     * Parse result has no restriction to deliver a browser switch result only once. After a parsed
     * result has been consumed, call {@link #clearActiveRequests(Context)} to enforce the same
     * "deliver once" behavior provided by {@link #deliverResult(FragmentActivity)}.
     *
     * @param context     The context used to check for pending browser switch requests
     * @param requestCode The request code for the matching pending request
     * @param intent      Intent to evaluate for deep link result
     * @return {@link BrowserSwitchResult} if one exists, null otherwise
     */
    @Nullable
    public BrowserSwitchResult parseResult(@NonNull Context context, int requestCode, @Nullable Intent intent) {
        BrowserSwitchResult result = null;
        if (intent != null && intent.getData() != null) {
            BrowserSwitchRequest request =
                    persistentStore.getActiveRequest(context.getApplicationContext());
            if (request != null && request.getRequestCode() == requestCode) {
                Uri deepLinkUrl = intent.getData();
                if (request.matchesDeepLinkUrlScheme(deepLinkUrl)) {
                    result = new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, request, deepLinkUrl);
                }
            }
        }
        return result;
    }

    /**
     * Parses and returns a browser switch result if a match is found for the given {@link BrowserSwitchRequest}
     * @param pendingRequest the {@link BrowserSwitchPendingRequest.Started} returned from
     * {@link BrowserSwitchClient#start(ComponentActivity, BrowserSwitchOptions)}
     * @param intent the intent to return to your application containing a deep link result from the
     *               browser flow
     * @return a {@link BrowserSwitchResult} if the browser switch was successfully completed, or
     * null if the user returned to the app without completing the browser switch
     */
    @Nullable
    public BrowserSwitchResult parseResult(@NonNull BrowserSwitchPendingRequest.Started pendingRequest, @Nullable Intent intent) {
        BrowserSwitchResult result = null;
        if (intent != null && intent.getData() != null) {
            Uri deepLinkUrl = intent.getData();
            if (pendingRequest.getBrowserSwitchRequest().matchesDeepLinkUrlScheme(deepLinkUrl)) {
                result = new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, pendingRequest.getBrowserSwitchRequest(), deepLinkUrl);
            }
        }
        return result;
    }

    /**
     * Clear singleton storage holding single pending browser switch request. Should be called after
     * a successful call to {@link #parseResult(Context, int, Intent)}
     *
     * @param context Context for storage to be cleared
     */
    public void clearActiveRequests(@NonNull Context context) {
        persistentStore.clearActiveRequest(context.getApplicationContext());
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
