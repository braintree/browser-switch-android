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
     * @return a {@link BrowserSwitchStartResult.Success} that should be stored and passed to
     * {@link BrowserSwitchClient#parseResult(Intent, String)} upon return to the app,
     * or {@link BrowserSwitchStartResult.Failure} if browser could not be launched.
     */
    @NonNull
    public BrowserSwitchStartResult start(@NonNull ComponentActivity activity, @NonNull BrowserSwitchOptions browserSwitchOptions) {
        // TODO: allow browser switching with application context
        try {
            assertCanPerformBrowserSwitch(activity, browserSwitchOptions);
        } catch (BrowserSwitchException e) {
            return new BrowserSwitchStartResult.Failure(e);
        }

        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();

        JSONObject metadata = browserSwitchOptions.getMetadata();

        if (activity.isFinishing()) {
            String activityFinishingMessage =
                    "Unable to start browser switch while host Activity is finishing.";
            return new BrowserSwitchStartResult.Failure(new BrowserSwitchException(activityFinishingMessage));
        } else {
            boolean launchAsNewTask = browserSwitchOptions.isLaunchAsNewTask();
            try {
                BrowserSwitchRequest request =
                        new BrowserSwitchRequest(requestCode, browserSwitchUrl, metadata, returnUrlScheme);
                customTabsInternalClient.launchUrl(activity, browserSwitchUrl, launchAsNewTask);
                return new BrowserSwitchStartResult.Success(request.toBase64EncodedJSON());
            } catch (ActivityNotFoundException | BrowserSwitchException e) {
                return new BrowserSwitchStartResult.Failure(new BrowserSwitchException("Unable to start browser switch without a web browser.", e));
            }
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
     * Parses and returns a browser switch result if a match is found for the given {@link BrowserSwitchRequest}
     *
     * @param intent              the intent to return to your application containing a deep link result from the
     *                            browser flow
     * @param pendingRequestState the {@link BrowserSwitchStartResult.Success} token returned from
     *                            {@link BrowserSwitchClient#start(ComponentActivity, BrowserSwitchOptions)}
     * @return a {@link BrowserSwitchParseResult.Success} if the browser switch was successfully
     * completed, or {@link BrowserSwitchParseResult.NoResult} if no result can be found for the given
     * {@link BrowserSwitchStartResult.Success}. A {@link BrowserSwitchParseResult.NoResult} will be
     * returned if the user returns to the app without completing the browser switch flow.
     */
    @NonNull
    public BrowserSwitchParseResult parseResult(@NonNull Intent intent, @NonNull String pendingRequestState) {
        if (intent != null && intent.getData() != null) {
            Uri deepLinkUrl = intent.getData();
            try {
                BrowserSwitchRequest pendingRequest =
                        BrowserSwitchRequest.fromBase64EncodedJSON(pendingRequestState);
                if (pendingRequest.matchesDeepLinkUrlScheme(deepLinkUrl)) {
                    return new BrowserSwitchParseResult.Success(deepLinkUrl, pendingRequest);
                }
            } catch (BrowserSwitchException e) {
                return new BrowserSwitchParseResult.Failure(e);
            }
        }
        return BrowserSwitchParseResult.NoResult.INSTANCE;
    }
}
