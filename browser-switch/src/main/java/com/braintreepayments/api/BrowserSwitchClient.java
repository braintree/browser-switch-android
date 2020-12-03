package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

public class BrowserSwitchClient {

    final private BrowserSwitchConfig config;
    final private ActivityFinder activityFinder;
    final private BrowserSwitchPersistentStore persistentStore;

    final private String returnUrlScheme;

    public static BrowserSwitchClient newInstance(String returnUrlScheme) {
        return new BrowserSwitchClient(
            BrowserSwitchConfig.newInstance(), ActivityFinder.newInstance(),
            BrowserSwitchPersistentStore.getInstance(), returnUrlScheme);
    }

    @VisibleForTesting
    static BrowserSwitchClient newInstance(
        BrowserSwitchConfig config, ActivityFinder activityFinder,
        BrowserSwitchPersistentStore persistentStore, String returnUrlScheme) {
        return new BrowserSwitchClient(config, activityFinder, persistentStore, returnUrlScheme);
    }

    private BrowserSwitchClient(
            BrowserSwitchConfig config, ActivityFinder activityFinder,
            BrowserSwitchPersistentStore persistentStore, String returnUrlScheme) {
        this.config = config;
        this.activityFinder = activityFinder;
        this.persistentStore = persistentStore;
        this.returnUrlScheme = returnUrlScheme;
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
        startSafe(activity, browserSwitchOptions);
    }

    private void startSafe(FragmentActivity activity, BrowserSwitchOptions browserSwitchOptions) {
        Context appContext = activity.getApplicationContext();

        Intent intent = config.createIntentToLaunchUriInBrowser(appContext, browserSwitchOptions.getUrl());
        int requestCode = browserSwitchOptions.getRequestCode();

        JSONObject metadata = browserSwitchOptions.getMetadata();
        BrowserSwitchRequest request = new BrowserSwitchRequest(
                requestCode, intent.getData(), BrowserSwitchRequest.PENDING, metadata);
        persistentStore.putActiveRequest(request, appContext);
        appContext.startActivity(intent);
    }

    void assertCanPerformBrowserSwitch(FragmentActivity activity, BrowserSwitchOptions browserSwitchOptions) throws BrowserSwitchException {
        Context appContext = activity.getApplicationContext();
        Intent intent = config.createIntentToLaunchUriInBrowser(appContext, browserSwitchOptions.getUrl());
        int requestCode = browserSwitchOptions.getRequestCode();

        String errorMessage = null;

        if (!isValidRequestCode(requestCode)) {
            errorMessage = "Request code cannot be Integer.MIN_VALUE";
        } else if (!isConfiguredForBrowserSwitch(appContext)) {
            errorMessage =
                "The return url scheme was not set up, incorrectly set up, " +
                "or more than one Activity on this device defines the same url " +
                "scheme in it's Android Manifest. See " +
                "https://github.com/braintree/browser-switch-android for more " +
                "information on setting up a return url scheme.";
        } else if (!canOpenUrl(appContext, intent)) {
            StringBuilder messageBuilder = new StringBuilder("No installed activities can open this URL");
            Uri uri = intent.getData();
            if (uri != null) {
                messageBuilder.append(String.format(": %s", uri.toString()));
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

    private boolean isConfiguredForBrowserSwitch(Context context) {
        Intent browserSwitchActivityIntent =
            config.createIntentForBrowserSwitchActivityQuery(returnUrlScheme);
        return activityFinder.canResolveActivityForIntent(context, browserSwitchActivityIntent);
    }

    private boolean canOpenUrl(Context context, Intent intent) {
        return activityFinder.canResolveActivityForIntent(context, intent);
    }

    /**
     * Deliver a pending browser switch result to an Android activity that is also a BrowserSwitchListener.
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * Cancel and Success results will be delivered only once. If there are no pending
     * browser switch results, this method does nothing.
     *
     * @param activity the BrowserSwitchListener that will receive a pending browser switch result
     */
    public void deliverResult(FragmentActivity activity) {
        if (activity instanceof BrowserSwitchCallback) {
            deliverResult(activity, (BrowserSwitchCallback) activity);
        } else {
            throw new IllegalArgumentException("Activity must implement BrowserSwitchListener.");
        }
    }

    /**
     * Deliver a pending browser switch result to an Android activity that is also a BrowserSwitchListener.
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * Cancel and Success results will be delivered only once. If there are no pending
     * browser switch results, this method does nothing.
     *
     * @param activity the BrowserSwitchListener that will receive a pending browser switch result
     * @param listener the listener that will receive browser switch callbacks
     */
    public void deliverResult(FragmentActivity activity, BrowserSwitchCallback listener) {
        Context appContext = activity.getApplicationContext();
        BrowserSwitchRequest request = persistentStore.getActiveRequest(appContext);
        if (request != null) {
            persistentStore.clearActiveRequest(appContext);
            int requestCode = request.getRequestCode();

            Uri uri = null;
            BrowserSwitchResult result;

            JSONObject metadata = request.getMetadata();
            if (request.getState().equalsIgnoreCase(BrowserSwitchRequest.SUCCESS)) {
                uri = request.getUri();
                result = new BrowserSwitchResult(BrowserSwitchResult.STATUS_OK, metadata);
            } else {
                result = new BrowserSwitchResult(BrowserSwitchResult.STATUS_CANCELED, metadata);
            }

            listener.onResult(requestCode, result, uri);
        }
    }

    /**
     * Capture a browser switch result that will later be delivered to the caller
     * (see {@link #deliverResult(FragmentActivity)}.
     *
     * @param intent intent for app link that called back into your application from browser
     * @param context Android context at time of capture
     */
    void captureResult(Context context, @Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        BrowserSwitchRequest request = persistentStore.getActiveRequest(context);
        if (request != null && uri != null) {
            request.setUri(uri);
            request.setState(BrowserSwitchRequest.SUCCESS);
            persistentStore.putActiveRequest(request, context);
        }
    }

    public String getReturnUrlScheme() {
        return returnUrlScheme;
    }
}
