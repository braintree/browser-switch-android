package com.braintreepayments.browserswitch;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.braintreepayments.browserswitch.test.TestBrowserSwitchFragment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchFragmentTest {

    private FragmentActivity activity;
    private ActivityController<FragmentActivity> controller;

    private BrowserSwitchFragment sut;
    private BrowserSwitchClient browserSwitchClient;

    @Before
    public void beforeEach() {
        browserSwitchClient = mock(BrowserSwitchClient.class);

        controller = Robolectric.buildActivity(FragmentActivity.class);
        controller.setup();

        activity = controller.get();

        // attach sut fragment to activity
        sut = new TestBrowserSwitchFragment();

        FragmentManager fm = activity.getSupportFragmentManager();
        fm.beginTransaction().add(sut, "test-fragment").commit();
    }

    @Test
    public void onResume_deliversBrowserSwitchResultViaClient() {
        sut.browserSwitchClient = browserSwitchClient;
        sut.onResume();

        verify(browserSwitchClient).deliverResult(sut);
    }

    @Test
    public void getReturnUrlScheme_returnsUrlSchemeUsingPackageNameFromContext() {
        String result = sut.getReturnUrlScheme();
        Assert.assertEquals(result, "com.braintreepayments.browserswitch.test.browserswitch");
    }

    @Test
    public void browserSwitchWithUri_startsBrowserSwitch() {
        sut.browserSwitchClient = browserSwitchClient;
        sut.browserSwitch(123, "https://example.com");

        Uri uri = Uri.parse("https://example.com");
        verify(browserSwitchClient).start(123, uri, sut);
    }

    @Test
    public void browserSwitchWithIntent_startsBrowserSwitch() {
        Intent intent = new Intent();

        sut.browserSwitchClient = browserSwitchClient;
        sut.browserSwitch(123, intent);

        verify(browserSwitchClient).start(123, intent, sut);
    }
}
