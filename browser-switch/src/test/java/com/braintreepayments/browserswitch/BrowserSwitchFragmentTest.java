package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.braintreepayments.browserswitch.test.TestActivity;
import com.braintreepayments.browserswitch.test.TestBrowserSwitchFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchFragmentTest {

    private TestActivity mActivity;
    private TestBrowserSwitchFragment mFragment;

    @Before
    public void setup() {
        BrowserSwitchActivity.clearReturnUri();

        mActivity = Robolectric.setupActivity(TestActivity.class);
        mFragment = new TestBrowserSwitchFragment();

        mActivity.getFragmentManager().beginTransaction()
                .add(mFragment, "test-fragment")
                .commit();
    }

    @Test
    public void onCreate_setsContext() {
        assertEquals(mActivity, mFragment.mContext);
    }

    @Test
    public void onCreate_restoresSavedInstanceState() {
        mFragment.mIsBrowserSwitching = true;
        Bundle bundle = new Bundle();
        mFragment.onSaveInstanceState(bundle);
        mFragment.mIsBrowserSwitching = false;

        mFragment.onCreate(bundle);

        assertTrue(mFragment.mIsBrowserSwitching);
    }

    @Test
    public void onResume_doesNothingIfNotBrowserSwitching() {
        mFragment.onResume();

        assertFalse(mFragment.onBrowserSwitchResultCalled);
    }

    @Test
    public void onResume_handlesBrowserSwitch() {
        mFragment.mIsBrowserSwitching = true;

        mFragment.onResume();

        assertTrue(mFragment.onBrowserSwitchResultCalled);
    }

    @Test
    public void onResume_clearsBrowserSwitchFlag() {
        mFragment.mIsBrowserSwitching = true;

        mFragment.onResume();

        assertFalse(mFragment.mIsBrowserSwitching);
    }

    @Test
    public void onResume_callsOnBrowserSwitchResultForCancels() {
        mFragment.mIsBrowserSwitching = true;

        mFragment.onResume();

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.CANCELED, mFragment.result);
        assertNull(mFragment.returnUri);
    }

    @Test
    public void onResume_callsOnBrowserSwitchResultForOk() {
        handleBrowserSwitchResponse("http://example.com/?key=value");
        mFragment.mIsBrowserSwitching = true;

        mFragment.onResume();

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.OK, mFragment.result);
        assertEquals("http://example.com/?key=value", mFragment.returnUri.toString());
    }

    @Test
    public void onResume_clearsReturnUris() {
        handleBrowserSwitchResponse("http://example.com/?key=value");
        assertEquals("http://example.com/?key=value", BrowserSwitchActivity.getReturnUri().toString());
        mFragment.mIsBrowserSwitching = true;

        mFragment.onResume();

        assertNull(BrowserSwitchActivity.getReturnUri());
    }

    @Test
    public void onSaveInstanceState_savesStateWhenNotBrowserSwitching() {
        Bundle bundle = new Bundle();
        mFragment.onSaveInstanceState(bundle);

        assertFalse(bundle.getBoolean("com.braintreepayments.browser_switch.EXTRA_BROWSER_SWITCHING"));
    }

    @Test
    public void onSaveInstanceState_savesStateWhenBrowserSwitching() {
        mFragment.mIsBrowserSwitching = true;

        Bundle bundle = new Bundle();
        mFragment.onSaveInstanceState(bundle);

        assertTrue(bundle.getBoolean("com.braintreepayments.browser_switch.EXTRA_BROWSER_SWITCHING"));
    }

    @Test
    public void getReturnUrlScheme_returnsUrlScheme() {
        assertEquals("org.robolectric.default.browserswitch", mFragment.getReturnUrlScheme());
    }

    @Test
    public void browserSwitch_withUrlSetsBrowserSwitchFlag() {
        mockContext(mock(Context.class));
        assertFalse(mFragment.mIsBrowserSwitching);

        mFragment.browserSwitch("http://example.com/");

        assertTrue(mFragment.mIsBrowserSwitching);
    }

    @Test
    public void browserSwitch_withUrlStartsBrowserSwitch() {
        Context context = mock(Context.class);
        mockContext(context);

        mFragment.browserSwitch("http://example.com/");

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(context).startActivity(captor.capture());
        assertEquals("http://example.com/", captor.getValue().getData().toString());
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, captor.getValue().getFlags());
    }

    @Test
    public void browserSwitch_withIntentSetsBrowserSwitchFlag() {
        mockContext(mock(Context.class));
        assertFalse(mFragment.mIsBrowserSwitching);

        mFragment.browserSwitch(new Intent());

        assertTrue(mFragment.mIsBrowserSwitching);
    }

    @Test
    public void browserSwitch_withIntentStartsBrowserSwitch() {
        Context context = mock(Context.class);
        mockContext(context);
        Intent intent = new Intent();
        mFragment.mContext = context;

        mFragment.browserSwitch(intent);

        verify(context).startActivity(intent);
    }

    private void mockContext(Context context) {
        when(context.getPackageName()).thenReturn("com.braintreepayments.browserswitch");
        PackageManager packageManager = mock(PackageManager.class);
        when(packageManager.queryIntentActivities(any(Intent.class), anyInt()))
                .thenReturn(Collections.singletonList(new ResolveInfo()));
        when(context.getPackageManager()).thenReturn(packageManager);
        mFragment.mContext = context;
    }

    private void handleBrowserSwitchResponse(String url) {
        Robolectric.buildActivity(BrowserSwitchActivity.class,
                new Intent(Intent.ACTION_VIEW, Uri.parse(url))).setup();
    }
}
