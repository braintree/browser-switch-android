package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.braintreepayments.api.browserswitch.R;

import org.json.JSONObject;

/**
 * Client that manages the logic for browser switching.
 */
public class BrowserSwitchClient {

    private final BrowserSwitchInspector browserSwitchInspector;

    private final ChromeCustomTabsInternalClient customTabsInternalClient;

    /**
     * Construct a client that manages the logic for browser switching.
     */
    public BrowserSwitchClient() {
        this(new BrowserSwitchInspector(), new ChromeCustomTabsInternalClient());
    }

    @VisibleForTesting
    BrowserSwitchClient(BrowserSwitchInspector browserSwitchInspector,
                        ChromeCustomTabsInternalClient customTabsInternalClient) {
        this.browserSwitchInspector = browserSwitchInspector;
        this.customTabsInternalClient = customTabsInternalClient;
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with a given set of {@link BrowserSwitchOptions} from an Android activity.
     *
     * @param activity             the activity used to start browser switch
     * @param browserSwitchOptions {@link BrowserSwitchOptions} the options used to configure the browser switch
     * @return a {@link BrowserSwitchStartResult.Started} that should be stored and passed to
     * {@link BrowserSwitchClient#completeRequest(Intent, String)} upon return to the app,
     * or {@link BrowserSwitchStartResult.Failure} if browser could not be launched.
     */
    @NonNull
    public BrowserSwitchStartResult start(@NonNull ComponentActivity activity, @NonNull BrowserSwitchOptions browserSwitchOptions) {
        try {
            assertCanPerformBrowserSwitch(activity, browserSwitchOptions);
        } catch (BrowserSwitchException e) {
            return new BrowserSwitchStartResult.Failure(e);
        }

        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();
        Uri appLinkUri = browserSwitchOptions.getAppLinkUri();

        JSONObject metadata = browserSwitchOptions.getMetadata();

        if (activity.isFinishing()) {
            String activityFinishingMessage =
                    "Unable to start browser switch while host Activity is finishing.";
            return new BrowserSwitchStartResult.Failure(new BrowserSwitchException(activityFinishingMessage));
        } else {
            boolean launchAsNewTask = browserSwitchOptions.isLaunchAsNewTask();
            BrowserSwitchRequest request;
            try {
                request = new BrowserSwitchRequest(
                        requestCode,
                        browserSwitchUrl,
                        metadata,
                        returnUrlScheme,
                        appLinkUri
                );
                customTabsInternalClient.launchUrl(activity, browserSwitchUrl, launchAsNewTask);
                return new BrowserSwitchStartResult.Started(request.toBase64EncodedJSON());
            } catch (ActivityNotFoundException | BrowserSwitchException e) {
                return new BrowserSwitchStartResult.Failure(new BrowserSwitchException("Unable to start browser switch without a web browser.", e));
            }
        }
    }

    /**
     * Throws a {@link BrowserSwitchException} when a browser switch flow cannot be started.
     *
     * @param activity             the activity used to start browser switch
     * @param browserSwitchOptions {@link BrowserSwitchOptions} the options used to configure the browser switch
     * @throws BrowserSwitchException exception containing the error message on why browser switch cannot be started
     */
    public void assertCanPerformBrowserSwitch(
            ComponentActivity activity,
            BrowserSwitchOptions browserSwitchOptions
    ) throws BrowserSwitchException {
        Context appContext = activity.getApplicationContext();

        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();

        String errorMessage = null;

        if (!isValidRequestCode(requestCode)) {
            errorMessage = activity.getString(R.string.error_request_code_invalid);
        } else if (returnUrlScheme == null && browserSwitchOptions.getAppLinkUri() == null) {
            errorMessage = activity.getString(R.string.error_app_link_uri_or_return_url_required);
        } else if (returnUrlScheme != null &&
                !browserSwitchInspector.isDeviceConfiguredForDeepLinking(appContext, returnUrlScheme)) {
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
     * Completes the browser switch flow and returns a browser switch result if a match is found for
     * the given {@link BrowserSwitchRequest}
     *
     * @param intent         the intent to return to your application containing a deep link result from the
     *                       browser flow
     * @param pendingRequest the pending request string returned from {@link BrowserSwitchStartResult.Started} via
     *                       {@link BrowserSwitchClient#start(ComponentActivity, BrowserSwitchOptions)}
     * @return a {@link BrowserSwitchFinalResult.Success} if the browser switch was successfully
     * completed, or {@link BrowserSwitchFinalResult.NoResult} if no result can be found for the given
     * pending request String. A {@link BrowserSwitchFinalResult.NoResult} will be
     * returned if the user returns to the app without completing the browser switch flow.
     */
    public BrowserSwitchFinalResult completeRequest(@NonNull Intent intent, @NonNull String pendingRequest) {
        if (intent != null && intent.getData() != null) {
            Uri returnUrl = intent.getData();

            try {
                BrowserSwitchRequest pr = BrowserSwitchRequest.fromBase64EncodedJSON(pendingRequest);
                if (returnUrl != null &&
                        (pr.matchesDeepLinkUrlScheme(returnUrl) || pr.matchesAppLinkUri(returnUrl))) {
                    return new BrowserSwitchFinalResult.Success(returnUrl, pr);
                }
            } catch (BrowserSwitchException e) {
                throw new RuntimeException(e);
            }
        }
        return BrowserSwitchFinalResult.NoResult.INSTANCE;
    }
}
