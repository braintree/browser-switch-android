package com.braintreepayments.api.browserswitch.demo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.braintreepayments.api.BrowserSwitchClient;
import com.braintreepayments.api.BrowserSwitchException;
import com.braintreepayments.api.BrowserSwitchOptions;
import com.braintreepayments.api.BrowserSwitchPendingRequest;
import com.braintreepayments.api.BrowserSwitchResult;
import com.braintreepayments.api.browserswitch.demo.utils.PendingRequestStore;

import java.util.Objects;

public class DemoActivitySingleTop extends AppCompatActivity {

    private static final String FRAGMENT_TAG = DemoFragment.class.getSimpleName();
    private static final String RETURN_URL_SCHEME = "my-custom-url-scheme-single-top";

    @VisibleForTesting
    BrowserSwitchClient browserSwitchClient = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        browserSwitchClient = new BrowserSwitchClient();

        FragmentManager fm = getSupportFragmentManager();
        if (getDemoFragment() == null) {
            fm.beginTransaction()
                    .add(android.R.id.content, new DemoFragment(), FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        BrowserSwitchPendingRequest.Started pendingRequest = PendingRequestStore.Companion.get(this);
        if (pendingRequest != null) {
            BrowserSwitchResult result = browserSwitchClient.completeRequest(pendingRequest, intent);
            if (result instanceof BrowserSwitchResult.Success) {
                Objects.requireNonNull(getDemoFragment()).onBrowserSwitchResult(((BrowserSwitchResult.Success) result).getResultInfo());
            }
            PendingRequestStore.Companion.clear(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BrowserSwitchPendingRequest.Started pendingRequest = PendingRequestStore.Companion.get(this);
        if (pendingRequest != null) {
            Objects.requireNonNull(getDemoFragment()).onBrowserSwitchError(new Exception("User did not complete browser switch"));
            PendingRequestStore.Companion.clear(this);
        }
    }

    public void startBrowserSwitch(BrowserSwitchOptions options) throws BrowserSwitchException {
        BrowserSwitchPendingRequest pendingRequest = browserSwitchClient.start(this, options);
        if (pendingRequest instanceof BrowserSwitchPendingRequest.Started) {
            PendingRequestStore.Companion.put(this,
                    (BrowserSwitchPendingRequest.Started) pendingRequest);
        } else if (pendingRequest instanceof BrowserSwitchPendingRequest.Failure) {
            Objects.requireNonNull(getDemoFragment()).onBrowserSwitchError(((BrowserSwitchPendingRequest.Failure) pendingRequest).getCause());
        }
    }

    private DemoFragment getDemoFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG);
        if (fragment instanceof DemoFragment) {
            return ((DemoFragment) fragment);
        }
        return null;
    }

    public String getReturnUrlScheme() {
        return RETURN_URL_SCHEME;
    }
}
