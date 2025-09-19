package com.braintreepayments.api;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.VisibleForTesting;
import androidx.browser.auth.AuthTabIntent;
import androidx.browser.customtabs.CustomTabsIntent;
import static androidx.browser.customtabs.CustomTabsIntent.OPEN_IN_BROWSER_STATE_OFF;
import androidx.browser.customtabs.ExperimentalOpenInBrowser;
import androidx.browser.trusted.TrustedWebActivityIntentBuilder;
import androidx.core.app.ActivityOptionsCompat;

class ChromeCustomTabsInternalClient {

    private final CustomTabsIntent.Builder customTabsIntentBuilder;
    private final AuthTabIntent.Builder authTabIntentBuilder;
    private ActivityResultLauncher<Intent> launcher;

    ChromeCustomTabsInternalClient(@NonNull ComponentActivity activity) {
        this(new CustomTabsIntent.Builder());
        launcher = AuthTabIntent.registerActivityResultLauncher(activity, this::handleAuthResult);
    }

    @VisibleForTesting
    ChromeCustomTabsInternalClient(CustomTabsIntent.Builder builder) {
        this.customTabsIntentBuilder = builder;
        this.authTabIntentBuilder = new AuthTabIntent.Builder();
    }

    void handleAuthResult(AuthTabIntent.AuthResult result) {
        String message = null;
        switch (result.resultCode) {
            case AuthTabIntent.RESULT_OK: message = "Received auth result."; break;
            case AuthTabIntent.RESULT_CANCELED: message = "AuthTab canceled."; break;
            case AuthTabIntent.RESULT_VERIFICATION_FAILED: message = "Verification failed."; break;
            case AuthTabIntent.RESULT_VERIFICATION_TIMED_OUT: message = "Verification timed out."; break;
            case AuthTabIntent.RESULT_UNKNOWN_CODE: message = "Unknown result code."; break;
        }

        if (result.resultCode == AuthTabIntent.RESULT_OK) {
            message += " Uri: " + result.resultUri;
        }

//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d("AuthTab = message is ", message);
    }

    @OptIn(markerClass = ExperimentalOpenInBrowser.class)
    void launchUrl(@NonNull ComponentActivity activity, Uri url, LaunchType launchType) throws ActivityNotFoundException {
//        ActivityResultLauncher<Intent> launcher = AuthTabIntent.registerActivityResultLauncher(activity, this::handleAuthResult);
        AuthTabIntent customTabsIntent = authTabIntentBuilder
                .setEphemeralBrowsingEnabled(true)
//                .setOpenInBrowserButtonState(OPEN_IN_BROWSER_STATE_OFF) // this was requested by a merchant, should
//                .setCloseButtonEnabled(false)
//                .setBackgroundInteractionEnabled(false)
//                .setCloseButtonPosition(CustomTabsIntent.CLOSE_BUTTON_POSITION_END)
//                .setToolbarCornerRadiusDp(10)
                // probably be a setting exposed to customize by the merchant.
                .build();
        if (launchType != null) {
            switch (launchType) {
                case ACTIVITY_NEW_TASK:
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    break;
                case ACTIVITY_CLEAR_TOP:
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    break;
            }
        }
        customTabsIntent.launch(launcher, url, url.getPathSegments().get(0));
    }


//    @OptIn(markerClass = ExperimentalOpenInBrowser.class)
//    void launchUrl(Context context, Uri url, LaunchType launchType) throws ActivityNotFoundException {
//        CustomTabsIntent customTabsIntent = customTabsIntentBuilder
//                .setEphemeralBrowsingEnabled(true)
//                .setOpenInBrowserButtonState(OPEN_IN_BROWSER_STATE_OFF) // this was requested by a merchant, should
//                .setCloseButtonEnabled(false)
//                .setBackgroundInteractionEnabled(false)
//                .setCloseButtonPosition(CustomTabsIntent.CLOSE_BUTTON_POSITION_END)
//                .setToolbarCornerRadiusDp(10)
//                // probably be a setting exposed to customize by the merchant.
//                .build();
//        if (launchType != null) {
//            switch (launchType) {
//                case ACTIVITY_NEW_TASK:
//                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    break;
//                case ACTIVITY_CLEAR_TOP:
//                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    break;
//            }
//        }
//        customTabsIntent.launchUrl(context, url);
//    }
}
