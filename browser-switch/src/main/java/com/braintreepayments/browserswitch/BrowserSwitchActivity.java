package com.braintreepayments.browserswitch;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

/**
 * <a href="https://developer.android.com/guide/topics/manifest/activity-element.html#lmode">singleTask</a>
 * Activity used to receive the response from a browser switch. This Activity contains no UI and
 * finishes during {@link Activity#onCreate(Bundle)}.
 */
public class BrowserSwitchActivity extends Activity {

    private static Uri sReturnUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // write request code to db
        BrowserSwitchRepository repository =
                BrowserSwitchRepository.newInstance(getApplication());
        // TODO: consider renaming to insertAsync
        repository.markPendingRequestAsFinished(BrowserSwitchConstants.PENDING_REQUEST_ID);

        sReturnUri = null;
        if (getIntent() != null && getIntent().getData() != null) {
            sReturnUri = getIntent().getData();
        }

        finish();
    }

    /**
     * @return the uri returned from the browser switch, or {@code null}.
     */
    @Nullable
    public static Uri getReturnUri() {
        return sReturnUri;
    }

    /**
     * Clears the return uri.
     */
    public static void clearReturnUri() {
        sReturnUri = null;
    }
}
