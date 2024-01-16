package com.braintreepayments.api.demo;

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
import com.braintreepayments.api.demo.utils.PendingRequestUtil;

import java.util.Objects;

public class DemoActivity extends AppCompatActivity {

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

        BrowserSwitchPendingRequest.Started pendingRequest = PendingRequestUtil.Companion.getPendingRequest(this);
        if (pendingRequest != null) {
            BrowserSwitchResult result = browserSwitchClient.parseResult(pendingRequest, intent);
            if (result != null) {
                Objects.requireNonNull(getDemoFragment()).onBrowserSwitchResult(result);
            }
            PendingRequestUtil.Companion.clearPendingRequest(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BrowserSwitchPendingRequest.Started pendingRequest = PendingRequestUtil.Companion.getPendingRequest(this);
        if (pendingRequest != null) {
            Objects.requireNonNull(getDemoFragment()).onBrowserSwitchError(new Exception("User did not complete browser switch"));
            PendingRequestUtil.Companion.clearPendingRequest(this);
        }
    }

    public void startBrowserSwitch(BrowserSwitchOptions options) throws BrowserSwitchException {
        BrowserSwitchPendingRequest pendingRequest = browserSwitchClient.start(this, options);
        if (pendingRequest instanceof BrowserSwitchPendingRequest.Started) {
            PendingRequestUtil.Companion.putPendingRequest(this,
                    (BrowserSwitchPendingRequest.Started) pendingRequest);
        } else if (pendingRequest instanceof BrowserSwitchPendingRequest.Failure) {
            getDemoFragment().onBrowserSwitchError(((BrowserSwitchPendingRequest.Failure) pendingRequest).getCause());
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
