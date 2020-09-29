package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

@SuppressWarnings("WeakerAccess")
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
     * with the given url from an Android fragment. The fragment must be attached to activity when invoking this method.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param uri the url to open.
     * @param fragment the fragment used to start browser switch. Must implement {@link BrowserSwitchListener}
     */
    public void start(int requestCode, Uri uri, Fragment fragment) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .requestCode(requestCode)
                .url(uri);

        start(browserSwitchOptions, fragment);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given url from an Android fragment. The fragment must be attached to activity when invoking this method.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param uri the url to open.
     * @param fragment the fragment used to start browser switch
     * @param listener the listener that will receive browser switch callbacks
     */
    public void start(int requestCode, Uri uri, Fragment fragment, BrowserSwitchListener listener) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .requestCode(requestCode)
                .url(uri);

        start(browserSwitchOptions, fragment, listener);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given url from an Android activity.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param uri the url to open.
     * @param activity the activity used to start browser switch. Must implement {@link BrowserSwitchListener}
     */
    public void start(int requestCode, Uri uri, FragmentActivity activity) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .requestCode(requestCode)
                .url(uri);

        start(browserSwitchOptions, activity);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given url from an Android activity.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param uri the url to open.
     * @param activity the activity used to start browser switch
     * @param listener the listener that will receive browser switch callbacks
     */
    public void start(int requestCode, Uri uri, FragmentActivity activity, BrowserSwitchListener listener) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .requestCode(requestCode)
                .url(uri);

        start(browserSwitchOptions, activity, listener);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given intent from an Android fragment. The fragment must be attached to activity when invoking this method.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param intent the intent to use to initiate a browser switch
     * @param fragment the fragment used to start browser switch. Must implement {@link BrowserSwitchListener}
     */
    public void start(int requestCode, Intent intent, Fragment fragment) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .intent(intent)
                .requestCode(requestCode);

        start(browserSwitchOptions, fragment);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given intent from an Android fragment. The fragment must be attached to activity when invoking this method.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param intent the intent to use to initiate a browser switch
     * @param fragment the fragment used to start browser switch
     * @param listener the listener that will receive browser switch callbacks
     */
    public void start(int requestCode, Intent intent, Fragment fragment, BrowserSwitchListener listener) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .intent(intent)
                .requestCode(requestCode);

        start(browserSwitchOptions, fragment, listener);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given intent from an Android activity.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param intent the intent to use to initiate a browser switch
     * @param activity the activity used to start browser switch. Must implement {@link BrowserSwitchListener}
     */
    public void start(int requestCode, Intent intent, FragmentActivity activity) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .intent(intent)
                .requestCode(requestCode);

        start(browserSwitchOptions, activity);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with the given intent from an Android activity.
     *
     * @param requestCode the request code used to differentiate requests from one another.
     * @param intent the intent to use to initiate a browser switch
     * @param activity the activity used to start browser switch
     * @param listener the listener that will receive browser switch callbacks
     */
    public void start(int requestCode, Intent intent, FragmentActivity activity, BrowserSwitchListener listener) {
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .intent(intent)
                .requestCode(requestCode);

        start(browserSwitchOptions, activity, listener);
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with a given set of {@link BrowserSwitchOptions} from an Android fragment. The fragment
     * must be attached to activity when invoking this method.
     *
     * @param browserSwitchOptions {@link BrowserSwitchOptions}
     * @param fragment the fragment used to start browser switch. Must implement {@link BrowserSwitchListener}
     */
    public void start(BrowserSwitchOptions browserSwitchOptions, Fragment fragment) {
        if (fragment instanceof BrowserSwitchListener) {
            start(browserSwitchOptions, fragment, (BrowserSwitchListener) fragment);
        } else {
            throw new IllegalArgumentException("Fragment must implement BrowserSwitchListener.");
        }
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with a given set of {@link BrowserSwitchOptions} from an Android fragment. The fragment
     * must be attached to activity when invoking this method.
     *
     * @param browserSwitchOptions {@link BrowserSwitchOptions}
     * @param fragment the fragment used to start browser switch
     * @param listener the listener that will receive browser switch callbacks
     */
    public void start(BrowserSwitchOptions browserSwitchOptions, Fragment fragment, BrowserSwitchListener listener) {
        FragmentActivity activity = fragment.getActivity();
        if (activity != null) {
            start(browserSwitchOptions, activity, listener);
        } else {
            throw new IllegalStateException("Fragment must be attached to an activity.");
        }
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with a given set of {@link BrowserSwitchOptions} from an Android activity.
     *
     * @param browserSwitchOptions {@link BrowserSwitchOptions}
     * @param activity the activity used to start browser switch. Must implement {@link BrowserSwitchListener}
     */
    public void start(BrowserSwitchOptions browserSwitchOptions, FragmentActivity activity) {
        if (activity instanceof BrowserSwitchListener) {
            start(browserSwitchOptions, activity, (BrowserSwitchListener) activity);
        } else {
            throw new IllegalArgumentException("Activity must implement BrowserSwitchListener.");
        }
    }

    /**
     * Open a browser or <a href="https://developer.chrome.com/multidevice/android/customtabs">Chrome Custom Tab</a>
     * with a given set of {@link BrowserSwitchOptions} from an Android activity.
     *
     * @param browserSwitchOptions {@link BrowserSwitchOptions}
     * @param activity the activity used to start browser switch
     * @param listener the listener that will receive browser switch callbacks
     */
    public void start(BrowserSwitchOptions browserSwitchOptions, FragmentActivity activity, BrowserSwitchListener listener) {
        Context appContext = activity.getApplicationContext();

        boolean useChromeCustomTabs = false;

        Intent intent = null;
        if(browserSwitchOptions.getIntent() != null) {
            intent = browserSwitchOptions.getIntent();
        } else {
            if (ChromeCustomTabs.isAvailable(appContext)) {
                useChromeCustomTabs = true;
            } else {
                intent = config.createIntentToLaunchUriInBrowser(appContext, browserSwitchOptions.getUrl());
            }
        }

        Uri targetUrl = null;
        if (useChromeCustomTabs) {
            targetUrl = browserSwitchOptions.getUrl();
        } else {
            targetUrl = intent.getData();
        }

        int requestCode = browserSwitchOptions.getRequestCode();

        String errorMessage;
        if (useChromeCustomTabs) {
            errorMessage = assertCanPerformBrowserSwitch(requestCode, appContext);
        } else {
            errorMessage = assertCanPerformBrowserSwitchWithIntent(requestCode, appContext, intent);
        }

        if (errorMessage == null) {
            JSONObject metadata = browserSwitchOptions.getMetadata();
            BrowserSwitchRequest request = new BrowserSwitchRequest(
                    requestCode, targetUrl, BrowserSwitchRequest.PENDING, metadata);
            persistentStore.putActiveRequest(request, appContext);

            if (useChromeCustomTabs) {
                ChromeCustomTabs.launchUrl(activity, browserSwitchOptions.getUrl());
            } else {
                appContext.startActivity(intent);
            }

        } else {
            BrowserSwitchResult result =
                new BrowserSwitchResult(BrowserSwitchResult.STATUS_ERROR, errorMessage);
            listener.onBrowserSwitchResult(requestCode, result, null);
        }
    }

    private String assertCanPerformBrowserSwitch(int requestCode, Context context) {
        String errorMessage = null;

        if (!isValidRequestCode(requestCode)) {
            errorMessage = "Request code cannot be Integer.MIN_VALUE";
        } else if (!isConfiguredForBrowserSwitch(context)) {
            errorMessage =
                "The return url scheme was not set up, incorrectly set up, " +
                "or more than one Activity on this device defines the same url " +
                "scheme in it's Android Manifest. See " +
                "https://github.com/braintree/browser-switch-android for more " +
                "information on setting up a return url scheme.";
        }
        return errorMessage;
    }

    private String assertCanPerformBrowserSwitchWithIntent(int requestCode, Context context, Intent intent) {
        String errorMessage = assertCanPerformBrowserSwitch(requestCode, context);

        if (errorMessage == null && !canOpenUrl(context, intent)) {
            StringBuilder messageBuilder = new StringBuilder("No installed activities can open this URL");
            Uri uri = intent.getData();
            if (uri != null) {
                messageBuilder.append(String.format(": %s", uri.toString()));
            }
            errorMessage = messageBuilder.toString();
        }
        return errorMessage;
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
     * Deliver a pending browser switch result to an Android fragment that is also a BrowserSwitchListener.
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * Cancel and Success results will be delivered only once. If there are no pending
     * browser switch results, this method does nothing.
     *
     * @param fragment the BrowserSwitchListener that will receive a pending browser switch result
     */
    public void deliverResult(Fragment fragment) {
        if (fragment instanceof BrowserSwitchListener) {
            deliverResult(fragment, (BrowserSwitchListener) fragment);
        } else {
            throw new IllegalArgumentException("Fragment must implement BrowserSwitchListener.");
        }
    }

    /**
     * Deliver a pending browser switch result to an Android fragment that is also a BrowserSwitchListener.
     * We recommend you call this method in onResume to receive a browser switch result once your
     * app has re-entered the foreground.
     *
     * @param fragment the BrowserSwitchListener that will receive a pending browser switch result
     * @param listener the listener that will receive browser switch callbacks
     */
    public void deliverResult(Fragment fragment, BrowserSwitchListener listener) {
        FragmentActivity activity = fragment.getActivity();
        if (activity != null) {
            deliverResult(activity, listener);
        } else {
            throw new IllegalStateException("Fragment must be attached to an activity.");
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
     */
    public void deliverResult(FragmentActivity activity) {
        if (activity instanceof BrowserSwitchListener) {
            deliverResult(activity, (BrowserSwitchListener) activity);
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
    public void deliverResult(FragmentActivity activity, BrowserSwitchListener listener) {
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
                result = new BrowserSwitchResult(
                        BrowserSwitchResult.STATUS_OK, null, metadata);
            } else {
                result = new BrowserSwitchResult(
                        BrowserSwitchResult.STATUS_CANCELED, null, metadata);
            }
            listener.onBrowserSwitchResult(requestCode, result, uri);
        }
    }

    /**
     * Capture a browser switch result that will later be delivered to the caller
     * (see {@link #deliverResult(FragmentActivity)} and {@link #deliverResult(Fragment)}).
     * @param intent intent for app link that called back into your application from browser
     * @param context Android context at time of capture
     */
    void captureResult(@Nullable Intent intent, Context context) {
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
}
