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
import com.braintreepayments.api.BrowserSwitchResult;

public class DemoActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG = DemoFragment.class.getSimpleName();

    @VisibleForTesting
    BrowserSwitchClient browserSwitchClient = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        browserSwitchClient = new BrowserSwitchClient();

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            fm.beginTransaction()
                    .add(android.R.id.content, new DemoFragment(), FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BrowserSwitchResult result = browserSwitchClient.deliverResult(this);
        if (result != null) {
            DemoFragment demoFragment = findDemoFragment();
            if (demoFragment != null) {
                demoFragment.onBrowserSwitchResult(result);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
    }

    public void startBrowserSwitch(BrowserSwitchOptions options) throws BrowserSwitchException {
        browserSwitchClient.start(this, options);
    }

    private DemoFragment findDemoFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG);
        if (fragment instanceof DemoFragment) {
            return ((DemoFragment) fragment);
        }
        return null;
    }
}
