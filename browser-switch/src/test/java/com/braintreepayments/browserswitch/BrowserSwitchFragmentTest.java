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
        assertEquals(mActivity.getApplicationContext(), mFragment.mContext);
    }

    @Test
    public void onCreate_doesNotOverrideContextIfAlreadySet() {
        mFragment = new TestBrowserSwitchFragment();
        Context context = mock(Context.class);
        mFragment.mContext = context;

        mActivity.getFragmentManager().beginTransaction()
                .add(mFragment, "test-fragment")
                .commit();

        assertEquals(context, mFragment.mContext);
    }

    @Test
    public void onCreate_restoresSavedInstanceState() {
        mFragment.mRequestCode = 42;
        Bundle bundle = new Bundle();
        mFragment.onSaveInstanceState(bundle);
        mFragment.mRequestCode = 0;

        mFragment.onCreate(bundle);

        assertEquals(42, mFragment.mRequestCode);
    }

    @Test
    public void onResume_doesNothingIfNotBrowserSwitching() {
        mFragment.onResume();

        assertFalse(mFragment.onBrowserSwitchResultCalled);
    }

    @Test
    public void onResume_handlesBrowserSwitch() {
        mFragment.mRequestCode = 42;

        mFragment.onResume();

        assertTrue(mFragment.onBrowserSwitchResultCalled);
    }

    @Test
    public void onResume_clearsBrowserSwitchRequestCode() {
        mFragment.mRequestCode = 42;

        mFragment.onResume();

        assertEquals(Integer.MIN_VALUE, mFragment.mRequestCode);
    }

    @Test
    public void onResume_callsOnBrowserSwitchResultForCancels() {
        mFragment.mRequestCode = 42;

        mFragment.onResume();

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(42, mFragment.requestCode);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.CANCELED, mFragment.result);
        assertNull(mFragment.returnUri);
    }

    @Test
    public void onResume_callsOnBrowserSwitchResultForOk() {
        handleBrowserSwitchResponse(42, "http://example.com/?key=value");

        mFragment.onResume();

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(42, mFragment.requestCode);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.OK, mFragment.result);
        assertEquals("http://example.com/?key=value", mFragment.returnUri.toString());
    }

    @Test
    public void onResume_clearsReturnUris() {
        handleBrowserSwitchResponse(42, "http://example.com/?key=value");
        assertEquals("http://example.com/?key=value", BrowserSwitchActivity.getReturnUri().toString());

        mFragment.onResume();

        assertNull(BrowserSwitchActivity.getReturnUri());
    }

    @Test
    public void onSaveInstanceState_savesStateWhenNotBrowserSwitching() {
        Bundle bundle = new Bundle();
        mFragment.onSaveInstanceState(bundle);

        assertEquals(Integer.MIN_VALUE,
                bundle.getInt("com.braintreepayments.browserswitch.EXTRA_REQUEST_CODE"));
    }

    @Test
    public void onSaveInstanceState_savesStateWhenBrowserSwitching() {
        mFragment.mRequestCode = 42;

        Bundle bundle = new Bundle();
        mFragment.onSaveInstanceState(bundle);

        assertEquals(42, bundle.getInt("com.braintreepayments.browserswitch.EXTRA_REQUEST_CODE"));
    }

    @Test
    public void getReturnUrlScheme_returnsUrlScheme() {
        assertEquals("org.robolectric.default.browserswitch", mFragment.getReturnUrlScheme());
    }

    @Test
    public void browserSwitch_setsRequestCode() {
        mockContext(mock(Context.class));
        assertEquals(Integer.MIN_VALUE, mFragment.mRequestCode);

        mFragment.browserSwitch(42, "http://example.com/");

        assertEquals(42, mFragment.mRequestCode);
    }

    @Test
    public void browserSwitch_withUrlStartsBrowserSwitch() {
        Context context = mock(Context.class);
        mockContext(context);

        mFragment.browserSwitch(42, "http://example.com/");

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(context).startActivity(captor.capture());
        assertEquals("http://example.com/", captor.getValue().getData().toString());
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, captor.getValue().getFlags());
    }

    @Test
    public void browserSwitch_withIntentSetsRequestCode() {
        mockContext(mock(Context.class));
        assertEquals(Integer.MIN_VALUE, mFragment.mRequestCode);

        mFragment.browserSwitch(42, new Intent());

        assertEquals(42, mFragment.mRequestCode);
    }

    @Test
    public void browserSwitch_withIntentStartsBrowserSwitch() {
        Context context = mock(Context.class);
        mockContext(context);
        Intent intent = new Intent();
        mFragment.mContext = context;

        mFragment.browserSwitch(42, intent);

        verify(context).startActivity(intent);
    }

    @Test
    public void browserSwitch_withUrlReturnsErrorForInvalidRequestCode() {
        Context context = mock(Context.class);
        mockContext(context);

        mFragment.browserSwitch(Integer.MIN_VALUE, "http://example.com/");

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(Integer.MIN_VALUE, mFragment.requestCode);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.ERROR, mFragment.result);
        assertEquals("Request code cannot be Integer.MIN_VALUE", mFragment.result.getErrorMessage());
        assertNull(mFragment.returnUri);
    }

    @Test
    public void browserSwitch_withIntentReturnsErrorForInvalidRequestCode() {
        Context context = mock(Context.class);
        mockContext(context);

        mFragment.browserSwitch(Integer.MIN_VALUE, new Intent());

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(Integer.MIN_VALUE, mFragment.requestCode);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.ERROR, mFragment.result);
        assertEquals("Request code cannot be Integer.MIN_VALUE", mFragment.result.getErrorMessage());
        assertNull(mFragment.returnUri);
    }

    @Test
    public void browserSwitch_withUrlReturnsErrorWhenReturnUrlSchemeIsNotSetupInAndroidManifest() {
        mFragment.browserSwitch(42, "http://example.com/");

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(42, mFragment.requestCode);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.ERROR, mFragment.result);
        assertEquals("The return url scheme was not set up, incorrectly set up, or more than one " +
                "Activity on this device defines the same url scheme in it's Android Manifest. " +
                "See https://github.com/braintree/browser-switch-android for more information on " +
                "setting up a return url scheme.", mFragment.result.getErrorMessage());
        assertNull(mFragment.returnUri);
    }

    @Test
    public void browserSwitch_withIntentReturnsErrorWhenReturnUrlSchemeIsNotSetupInAndroidManifest() {
        mFragment.browserSwitch(42, new Intent());

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(42, mFragment.requestCode);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.ERROR, mFragment.result);
        assertEquals("The return url scheme was not set up, incorrectly set up, or more than one " +
                "Activity on this device defines the same url scheme in it's Android Manifest. " +
                "See https://github.com/braintree/browser-switch-android for more information on " +
                "setting up a return url scheme.", mFragment.result.getErrorMessage());
        assertNull(mFragment.returnUri);
    }

    private void mockContext(Context context) {
        when(context.getPackageName()).thenReturn("com.braintreepayments.browserswitch");
        PackageManager packageManager = mock(PackageManager.class);
        when(packageManager.queryIntentActivities(any(Intent.class), anyInt()))
                .thenReturn(Collections.singletonList(new ResolveInfo()));
        when(context.getPackageManager()).thenReturn(packageManager);
        mFragment.mContext = context;
    }

    private void handleBrowserSwitchResponse(int requestCode, String url) {
        Robolectric.buildActivity(BrowserSwitchActivity.class,
                new Intent(Intent.ACTION_VIEW, Uri.parse(url))).setup();
        mFragment.mRequestCode = requestCode;
    }
}
