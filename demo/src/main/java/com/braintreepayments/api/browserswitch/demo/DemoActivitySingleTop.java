package com.braintreepayments.api.browserswitch.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.braintreepayments.api.BrowserSwitchClient;
import com.braintreepayments.api.BrowserSwitchFinalResult;
import com.braintreepayments.api.BrowserSwitchException;
import com.braintreepayments.api.BrowserSwitchOptions;
import com.braintreepayments.api.BrowserSwitchStartResult;
import com.braintreepayments.api.browserswitch.demo.utils.PendingRequestStore;

import java.util.Objects;

public class DemoActivitySingleTop extends AppCompatActivity {

    private static final String FRAGMENT_TAG = DemoFragment.class.getSimpleName();
    private static final String RETURN_URL_SCHEME = "my-custom-url-scheme-single-top";

    @VisibleForTesting
    BrowserSwitchClient browserSwitchClient = null;

    private boolean useAuthTab = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        browserSwitchClient = new BrowserSwitchClient();
        browserSwitchClient.initializeAuthTabLauncher(this, this::handleBrowserSwitchResult);

        if (browserSwitchClient.isAuthTabSupported(this)) {
            useAuthTab = true;
        }

        FragmentManager fm = getSupportFragmentManager();
        if (getDemoFragment() == null) {
            fm.beginTransaction()
                    .add(android.R.id.content, new DemoFragment(), FRAGMENT_TAG)
                    .commit();
        }

        // Support Edge-to-Edge layout in Android 15
        View navHostView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(navHostView, (v, insets) -> {
            @WindowInsetsCompat.Type.InsetsType int insetTypeMask =
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.systemGestures();
            Insets bars = insets.getInsets(insetTypeMask);
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Only handle Custom Tabs fallback case
        if (!useAuthTab) {
            String pendingRequest = PendingRequestStore.get(this);
            if (pendingRequest != null) {
                BrowserSwitchFinalResult result =
                        browserSwitchClient.completeRequest(intent, pendingRequest);
                handleBrowserSwitchResult(result);
                PendingRequestStore.clear(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only check for incomplete browser switch in Custom Tabs mode
        if (!useAuthTab) {
            String pendingRequest = PendingRequestStore.get(this);
            if (pendingRequest != null) {
                Objects.requireNonNull(getDemoFragment())
                        .onBrowserSwitchError(new Exception("User did not complete browser switch"));
                PendingRequestStore.clear(this);
            }
        }
    }

    private void handleBrowserSwitchResult(BrowserSwitchFinalResult result) {
        if (result instanceof BrowserSwitchFinalResult.Success) {
            Objects.requireNonNull(getDemoFragment())
                    .onBrowserSwitchResult((BrowserSwitchFinalResult.Success) result);
        } else if (result instanceof BrowserSwitchFinalResult.NoResult) {
            Objects.requireNonNull(getDemoFragment())
                    .onBrowserSwitchError(new Exception("User did not complete browser switch"));
        } else if (result instanceof BrowserSwitchFinalResult.Failure) {
            Objects.requireNonNull(getDemoFragment())
                    .onBrowserSwitchError(((BrowserSwitchFinalResult.Failure) result).getError());
        }
    }

    public void startBrowserSwitch(BrowserSwitchOptions options) throws BrowserSwitchException {
        BrowserSwitchStartResult result = browserSwitchClient.start(this, options);
        if (result instanceof BrowserSwitchStartResult.Started) {
            // Only store pending request for Custom Tabs fallback
            if (!useAuthTab) {
                PendingRequestStore.put(this,
                        ((BrowserSwitchStartResult.Started) result).getPendingRequest());
            }
        } else if (result instanceof BrowserSwitchStartResult.Failure) {
            Objects.requireNonNull(getDemoFragment())
                    .onBrowserSwitchError(((BrowserSwitchStartResult.Failure) result).getError());
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