package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.browser.auth.AuthTabIntent;

import com.braintreepayments.api.browserswitch.R;

import org.json.JSONObject;

/**
 * Client that manages the logic for browser switching.
 */
public class BrowserSwitchClient {

    private final BrowserSwitchInspector browserSwitchInspector;
    private final AuthTabInternalClient authTabInternalClient;
    private ActivityResultLauncher<Intent> authTabLauncher;
    private BrowserSwitchRequest pendingAuthTabRequest;

    /**
     * Construct a client that manages the logic for browser switching.
     */
    public BrowserSwitchClient() {
        this(new BrowserSwitchInspector(), new AuthTabInternalClient());
    }

    @VisibleForTesting
    BrowserSwitchClient(BrowserSwitchInspector browserSwitchInspector,
                        AuthTabInternalClient authTabInternalClient) {
        this.browserSwitchInspector = browserSwitchInspector;
        this.authTabInternalClient = authTabInternalClient;
    }

    /**
     * Initialize the Auth Tab launcher. This should be called in the activity's onCreate()
     * before the activity is started.
     */
    public void initializeAuthTabLauncher(@NonNull ComponentActivity activity,
                                          @NonNull AuthTabCallback callback) {
        this.authTabLauncher = AuthTabIntent.registerActivityResultLauncher(
                activity,
                result -> {
                    BrowserSwitchFinalResult finalResult;

                    switch (result.resultCode) {
                        case AuthTabIntent.RESULT_OK:
                            if (result.resultUri != null && pendingAuthTabRequest != null) {
                                finalResult = new BrowserSwitchFinalResult.Success(
                                        result.resultUri,
                                        pendingAuthTabRequest
                                );
                            } else {
                                finalResult = BrowserSwitchFinalResult.NoResult.INSTANCE;
                            }
                            break;
                        case AuthTabIntent.RESULT_CANCELED:
                            finalResult = BrowserSwitchFinalResult.NoResult.INSTANCE;
                            break;
                        case AuthTabIntent.RESULT_VERIFICATION_FAILED:
                            finalResult = BrowserSwitchFinalResult.NoResult.INSTANCE;
                            break;
                        case AuthTabIntent.RESULT_VERIFICATION_TIMED_OUT:
                            finalResult = BrowserSwitchFinalResult.NoResult.INSTANCE;
                            break;
                        default:
                            finalResult = BrowserSwitchFinalResult.NoResult.INSTANCE;
                    }
                    callback.onResult(finalResult);
                    pendingAuthTabRequest = null;
                }
        );
    }

    /**
     * Open a browser or Auth Tab with a given set of {@link BrowserSwitchOptions} from an Android activity.
     *
     * @param activity             the activity used to start browser switch
     * @param browserSwitchOptions {@link BrowserSwitchOptions} the options used to configure the browser switch
     * @return a {@link BrowserSwitchStartResult.Started} that should be stored and passed to
     * {@link BrowserSwitchClient#completeRequest(Intent, String)} upon return to the app (for Custom Tabs fallback),
     * or {@link BrowserSwitchStartResult.Failure} if browser could not be launched.
     */
    @NonNull
    public BrowserSwitchStartResult start(@NonNull ComponentActivity activity,
                                          @NonNull BrowserSwitchOptions browserSwitchOptions) {
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
        }

        LaunchType launchType = browserSwitchOptions.getLaunchType();
        BrowserSwitchRequest request;

        try {
            request = new BrowserSwitchRequest(
                    requestCode,
                    browserSwitchUrl,
                    metadata,
                    returnUrlScheme,
                    appLinkUri
            );

            boolean useAuthTab = authTabInternalClient.isAuthTabSupported(activity);

            if (useAuthTab) {
                this.pendingAuthTabRequest = request;
            }

            authTabInternalClient.launchUrl(
                    activity,
                    browserSwitchUrl,
                    returnUrlScheme,
                    appLinkUri,
                    authTabLauncher,
                    launchType
            );

            return new BrowserSwitchStartResult.Started(request.toBase64EncodedJSON());

        } catch (ActivityNotFoundException e) {
            this.pendingAuthTabRequest = null;
            return new BrowserSwitchStartResult.Failure(
                    new BrowserSwitchException("Unable to start browser switch without a web browser.", e)
            );
        } catch (Exception e) {
            this.pendingAuthTabRequest = null;
            return new BrowserSwitchStartResult.Failure(
                    new BrowserSwitchException("Unable to start browser switch: " + e.getMessage(), e)
            );
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
     * Completes the browser switch flow for Custom Tabs fallback scenarios.
     * This method is still needed for devices that don't support Auth Tab.
     *
     * @param intent         the intent to return to your application containing a deep link result
     * @param pendingRequest the pending request string returned from {@link BrowserSwitchStartResult.Started}
     * @return a {@link BrowserSwitchFinalResult}
     */
    public BrowserSwitchFinalResult completeRequest(@NonNull Intent intent, @NonNull String pendingRequest) {
        if (intent.getData() != null) {
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

    public boolean isAuthTabSupported(Context context) {
        return authTabInternalClient.isAuthTabSupported(context);
    }
}