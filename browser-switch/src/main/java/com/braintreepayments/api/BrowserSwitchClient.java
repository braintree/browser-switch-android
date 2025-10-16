package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    @Nullable
    private BrowserSwitchFinalResult authTabCallbackResult;

    /**
     * Construct a client that manages browser switching with Chrome Custom Tabs fallback only.
     * This constructor does not initialize Auth Tab support. For Auth Tab functionality,
     * use {@link #BrowserSwitchClient(ActivityResultCaller)} instead.
     */
    public BrowserSwitchClient() {
        this(new BrowserSwitchInspector(), new AuthTabInternalClient());
    }

    /**
     * Construct a client that manages the logic for browser switching and automatically
     * initializes the Auth Tab launcher.
     *
     * <p>IMPORTANT: This constructor enables the AuthTab functionality, which has several caveats:
     *
     * <ul>
     *   <li><strong>This constructor must be called in the activity/fragment's {@code onCreate()} method</strong>
     *       to properly register the activity result launcher before the activity/fragment is started.
     *   <li>The caller must be an {@link ActivityResultCaller} to register for activity results.
     *   <li>{@link LaunchType#ACTIVITY_NEW_TASK} is not supported when using AuthTab and will be ignored.
     *       Only {@link LaunchType#ACTIVITY_CLEAR_TOP} is supported with AuthTab.
     *   <li>When using SingleTop activities, you must check for launcher results in {@code onResume()} as well
     *       as in {@code onNewIntent()}, since the AuthTab activity result might be delivered during the
     *       resuming phase.
     *   <li>Care must be taken to avoid calling {@link #completeRequest(Intent, String)} multiple times
     *       for the same result. Merchants should properly track their pending request state to ensure
     *       the completeRequest method is only called once per browser switch session.
     *   <li>AuthTab support is <strong>browser version dependent</strong>. It requires Chrome version 137
     *       or higher on the user's device. On devices with older browser versions, the library will
     *       automatically fall back to Custom Tabs. This means that enabling AuthTab is not guaranteed
     *       to use the AuthTab flow if the user's browser version is too old.
     * </ul>
     *
     * <p>Consider using the default constructor {@link #BrowserSwitchClient()} if these limitations
     * are incompatible with your implementation.
     *
     * @param caller The ActivityResultCaller used to initialize the Auth Tab launcher.
     */
    public BrowserSwitchClient(@NonNull ActivityResultCaller caller) {
        this(new BrowserSwitchInspector(), new AuthTabInternalClient());
        initializeAuthTabLauncher(caller);
    }

    @VisibleForTesting
    BrowserSwitchClient(BrowserSwitchInspector browserSwitchInspector,
                        AuthTabInternalClient authTabInternalClient) {
        this.browserSwitchInspector = browserSwitchInspector;
        this.authTabInternalClient = authTabInternalClient;
    }

    @VisibleForTesting
    BrowserSwitchClient(@NonNull ActivityResultCaller caller,
                        BrowserSwitchInspector inspector,
                        AuthTabInternalClient internal) {
        this(inspector, internal);
        initializeAuthTabLauncher(caller);
    }

    /**
     * Initialize the Auth Tab launcher. This should be called in the activity/fragment's onCreate()
     * before it is started.
     *
     * @param caller The ActivityResultCaller (Activity or Fragment) used to initialize the Auth Tab launcher
     */
   private void initializeAuthTabLauncher(@NonNull ActivityResultCaller caller) {

        this.authTabLauncher = AuthTabIntent.registerActivityResultLauncher(
                caller,
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
                        default:
                            finalResult = BrowserSwitchFinalResult.NoResult.INSTANCE;
                    }
                    this.authTabCallbackResult = finalResult;
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

        this.authTabCallbackResult = null;

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

            boolean useAuthTab = isAuthTabSupported(activity);

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
     * Completes the browser switch flow for both Auth Tab and Custom Tabs fallback scenarios.
     * This method first checks if we have a result from the Auth Tab callback,
     * and returns it if available. Otherwise, it follows the Custom Tabs flow.
     *
     * <p>See <a href="https://developer.chrome.com/docs/android/custom-tabs/guide-auth-tab#fallback_to_custom_tabs">
     * Auth Tab Fallback Documentation</a> for details on when Custom Tabs fallback is required
     *
     * <p><strong>IMPORTANT:</strong> When using Auth Tab with SingleTop activities, you must call this method
     * in both {@code onNewIntent()} <em>and</em> {@code onResume()} to ensure the result is properly processed
     * regardless of which launch mode is used.
     *
     * @param intent         the intent to return to your application containing a deep link result
     * @param pendingRequest the pending request string returned from {@link BrowserSwitchStartResult.Started}
     * @return a {@link BrowserSwitchFinalResult}
     */
    public BrowserSwitchFinalResult completeRequest(@NonNull Intent intent, @NonNull String pendingRequest) {
        if (authTabCallbackResult != null) {
            BrowserSwitchFinalResult result = authTabCallbackResult;
            authTabCallbackResult = null;
            return result;
        } else if (intent.getData() != null) {
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

    /**
     * Checks if Auth Tab is supported on this device and if the launcher has been initialized.
     * @param context The application context
     * @return true if Auth Tab is supported by the browser AND the launcher has been initialized,
     *         false otherwise
     */
    @VisibleForTesting
    boolean isAuthTabSupported(Context context) {
        return authTabLauncher != null && authTabInternalClient.isAuthTabSupported(context);
    }
}