package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.braintreepayments.browserswitch.test.TestFragmentActivity;
import com.braintreepayments.browserswitch.test.TestBrowserSwitchFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchFragmentTest {

    private TestFragmentActivity mActivity;
    private TestBrowserSwitchFragment mFragment;

    @Before
    public void setup() {
        BrowserSwitchActivity.clearReturnUri();

        mActivity = Robolectric.setupActivity(TestFragmentActivity.class);
        mFragment = new TestBrowserSwitchFragment();

        Context mockContext = mock(Context.class);

        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockContext.getPackageName()).thenReturn("com.braintreepayments.browserswitch");

        mFragment.mContext = mockContext;

        mActivity.getFragmentManager().beginTransaction()
                .add(mFragment, "test-fragment")
                .commit();
    }

    @Test
    public void onCreate_setsContext() {
        BrowserSwitchFragment fragment = new TestBrowserSwitchFragment();

        mActivity.getFragmentManager().beginTransaction()
                .add(fragment, "test-fragment")
                .commit();

        assertEquals(mActivity.getApplicationContext(), fragment.mContext);
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
        mockContext(returnIntent());
        mFragment.browserSwitch(42, "http://example.com");

        mFragment.onResume();

        assertTrue(mFragment.onBrowserSwitchResultCalled);
    }

    @Test
    public void onResume_clearsBrowserSwitchRequestCode() {
        mockContext(returnIntent());
        mFragment.browserSwitch(42, "http://example.com");

        mFragment.onResume();

        assertEquals(Integer.MIN_VALUE, mFragment.mRequestCode);
    }

    @Test
    public void onResume_callsOnBrowserSwitchResultForCancels() {
        mockContext(returnIntent());
        mockContext(switchIntent("http://example.com"));

        mFragment.browserSwitch(42, "http://example.com");

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
        assertEquals("com.braintreepayments.browserswitch.browserswitch", mFragment.getReturnUrlScheme());
    }

    @Test
    public void browserSwitch_setsRequestCode() {
        mockContext(returnIntent());
        mockContext(switchIntent("http://example.com/"));
        assertEquals(Integer.MIN_VALUE, mFragment.mRequestCode);

        mFragment.browserSwitch(42, "http://example.com/");

        assertEquals(42, mFragment.mRequestCode);
    }

    @Test
    public void browserSwitch_withUrlStartsBrowserSwitch() {
        mockContext(returnIntent());
        mockContext(switchIntent("http://example.com/"));

        mFragment.browserSwitch(42, "http://example.com/");

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(mFragment.mContext).startActivity(captor.capture());
        assertEquals("http://example.com/", captor.getValue().getData().toString());
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, captor.getValue().getFlags());
    }

    @Test
    public void browserSwitch_withIntentSetsRequestCode() {
        mockContext(returnIntent());
        mockContext(switchIntent("http://example.com/"));
        assertEquals(Integer.MIN_VALUE, mFragment.mRequestCode);

        mFragment.browserSwitch(42, switchIntent("http://example.com/"));

        assertEquals(42, mFragment.mRequestCode);
    }

    @Test
    public void browserSwitch_withIntentStartsBrowserSwitch() {
        Intent switchIntent = switchIntent("http://example.com/");

        mockContext(returnIntent());
        mockContext(switchIntent);

        mFragment.browserSwitch(42, switchIntent);

        verify(mFragment.mContext).startActivity(switchIntent);
    }

    @Test
    public void browserSwitch_withUrlReturnsErrorForInvalidRequestCode() {
        Context context = mock(Context.class);
        mockContext(returnIntent());

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
        mockContext(returnIntent());

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

    @Test
    public void browserSwitch_returnsErrorWhenNoActivitiesAvailableToHandleIntent() {
        mockContext(returnIntent());

        Intent browserSwitchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com/"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        when(mFragment.mContext.getPackageManager().queryIntentActivities(eq(browserSwitchIntent), anyInt()))
                .thenReturn(Collections.<ResolveInfo>emptyList());

        mFragment.browserSwitch(42, browserSwitchIntent);

        assertTrue(mFragment.onBrowserSwitchResultCalled);
        assertEquals(42, mFragment.requestCode);
        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.ERROR, mFragment.result);
        assertEquals("No installed activities can open this URL: http://example.com/", mFragment.result.getErrorMessage());
        assertNull(mFragment.returnUri);
    }

    @Test
    public void browserSwitchResultConvert_whenPassedOK_converts() {
        BrowserSwitchResult browserSwitchResult = BrowserSwitchResult.OK
                .setErrorMessage("Error Message is OK");

        BrowserSwitchFragment.BrowserSwitchResult result = BrowserSwitchFragment
                .BrowserSwitchResult.convert(browserSwitchResult);

        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.OK, result);
        assertEquals("Error Message is OK", result.getErrorMessage());
    }

    @Test
    public void browserSwitchResultConvert_whenPassedCANCELED_converts() {
        BrowserSwitchResult browserSwitchResult = BrowserSwitchResult.CANCELED
                .setErrorMessage("Error Message is CANCELED");

        BrowserSwitchFragment.BrowserSwitchResult result = BrowserSwitchFragment
                .BrowserSwitchResult.convert(browserSwitchResult);

        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.CANCELED, result);
        assertEquals("Error Message is CANCELED", result.getErrorMessage());
    }

    @Test
    public void browserSwitchResultConvert_whenPassedERROR_converts() {
        BrowserSwitchResult browserSwitchResult = BrowserSwitchResult.ERROR
                .setErrorMessage("Error Message is ERROR");

        BrowserSwitchFragment.BrowserSwitchResult result = BrowserSwitchFragment
                .BrowserSwitchResult.convert(browserSwitchResult);

        assertEquals(BrowserSwitchFragment.BrowserSwitchResult.ERROR, result);
        assertEquals("Error Message is ERROR", result.getErrorMessage());
    }

    private void mockContext(final Intent intent) {
        ArgumentMatcher<Intent> intentMatcher = new ArgumentMatcher<Intent>() {
            @Override
            public boolean matches(Intent argument) {
                return argument != null && intent.getData().equals(argument.getData());
            }
        };

        when(mFragment.mContext.getPackageManager().queryIntentActivities(argThat(intentMatcher), anyInt()))
                .thenReturn(Collections.singletonList(new ResolveInfo()));
    }

    private void handleBrowserSwitchResponse(int requestCode, String url) {
        Robolectric.buildActivity(BrowserSwitchActivity.class,
                new Intent(Intent.ACTION_VIEW, Uri.parse(url))).setup();
        mFragment.mRequestCode = requestCode;
    }

    private Intent returnIntent() {
        return new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(mFragment.getReturnUrlScheme() + "://"))
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addCategory(Intent.CATEGORY_BROWSABLE);
    }

    private Intent switchIntent(String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
}
