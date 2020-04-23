package com.braintreepayments.browserswitch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <a href="https://developer.android.com/guide/topics/manifest/activity-element.html#lmode">singleTask</a>
 * Activity used to receive the response from a browser switch. This Activity contains no UI and
 * finishes during {@link Activity#onCreate(Bundle)}.
 */
public class BrowserSwitchActivity extends Activity {

    private static final String PREF_FILE = "BraintreeBrowserSwitch";
    private static final String PREF_KEY_RETURN_URI = "browser_switch_return_uri";
    private static Intent sReturnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clearReturnUri(this);
        if (getIntent() != null && getIntent().getData() != null) {
            Uri returnUri = getIntent().getData();
            getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREF_KEY_RETURN_URI, returnUri.toString())
                    .apply();
        }

        if (sReturnIntent != null) {
            Intent relaunchActivityIntent = new Intent(sReturnIntent)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(relaunchActivityIntent);
        }
        finish();
    }

    public static void setReturnIntent(@NonNull Intent returnIntent) {
        sReturnIntent = returnIntent;
    }

    public static void clearReturnIntent() {
        sReturnIntent = null;
    }

    /**
     * @return the uri returned from the browser switch, or {@code null}.
     */
    @Nullable
    public static Uri getReturnUri(Context context) {
        String returnUriString =
                context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                        .getString(PREF_KEY_RETURN_URI, null);
        if (!TextUtils.isEmpty(returnUriString)) {
            return Uri.parse(returnUriString);
        } else {
            return null;
        }
    }

    /**
     * Clears the return uri.
     */
    public static void clearReturnUri(Context context) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .edit()
                .remove(PREF_KEY_RETURN_URI)
                .apply();
    }
}
