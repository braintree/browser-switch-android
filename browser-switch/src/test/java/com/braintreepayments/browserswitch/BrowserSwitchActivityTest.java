package com.braintreepayments.browserswitch;

import android.content.Intent;
import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchActivityTest {

    private ActivityController<BrowserSwitchActivity> mActivityController;
    private BrowserSwitchActivity mActivity;

    @Test
    public void finishesInOnCreate() {
        mActivityController = Robolectric.buildActivity(BrowserSwitchActivity.class);
        mActivity = mActivityController.setup().get();

        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void returnUriIsNullIfIntentIsNull() {
        mActivityController = Robolectric.buildActivity(BrowserSwitchActivity.class);
        mActivity = mActivityController.setup().get();

        assertNull(BrowserSwitchActivity.getReturnUri());
        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void returnUriIsNullIfDataIsNull() {
        mActivityController = Robolectric.buildActivity(BrowserSwitchActivity.class, new Intent());
        mActivity = mActivityController.setup().get();

        assertNull(BrowserSwitchActivity.getReturnUri());
        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void returnUriIsUriFromIntent() {
        setup("http://example.com?key=value");

        assertEquals("http://example.com?key=value", BrowserSwitchActivity.getReturnUri().toString());
        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void getReturnUri_returnsUri() {
        setup("http://example.com?key=value");

        assertEquals("http://example.com?key=value", BrowserSwitchActivity.getReturnUri().toString());
        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void clearReturnUri_clearsReturnUri() {
        setup("http://example.com?key=value");
        assertNotNull(BrowserSwitchActivity.getReturnUri());

        BrowserSwitchActivity.clearReturnUri();

        assertNull(BrowserSwitchActivity.getReturnUri());
    }

    private void setup(String url) {
        mActivityController = Robolectric.buildActivity(BrowserSwitchActivity.class,
                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        mActivity = mActivityController.setup().get();
    }
}
