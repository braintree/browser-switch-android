package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrowserSwitchClientUnitTest {

    static abstract class ActivityListener extends FragmentActivity implements BrowserSwitchListener {}

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private BrowserSwitchPersistentStore persistentStore;
    private BrowserSwitchInspector browserSwitchInspector;
    private CustomTabsIntent.Builder customTabsIntentBuilder;

    private Uri uri;
    private FragmentActivity plainActivity;
    private ActivityListener activityAndListener;

    private Context applicationContext;
    private BrowserSwitchListener browserSwitchListener;

    private String returnUrlScheme;
    private CustomTabsIntent customTabsIntent;

    @Before
    public void beforeEach() {
        persistentStore = mock(BrowserSwitchPersistentStore.class);

        browserSwitchInspector = mock(BrowserSwitchInspector.class);
        customTabsIntentBuilder = mock(CustomTabsIntent.Builder.class);

        uri = mock(Uri.class);

        plainActivity = mock(FragmentActivity.class);
        activityAndListener = mock(ActivityListener.class);

        applicationContext = mock(Context.class);
        browserSwitchListener = mock(BrowserSwitchListener.class);

        returnUrlScheme = "sample-url-scheme";
        customTabsIntent = mock(CustomTabsIntent.class);

        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);
        when(customTabsIntentBuilder.build()).thenReturn(customTabsIntent);
    }

    @Test
    public void start_createsBrowserSwitchIntentAndInitiatesBrowserSwitch() throws BrowserSwitchException {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, uri)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(uri)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);
        sut.start(plainActivity, options);

        verify(customTabsIntent).launchUrl(plainActivity, uri);

        ArgumentCaptor<BrowserSwitchRequest> captor =
                ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(captor.capture(), same(applicationContext));

        BrowserSwitchRequest browserSwitchRequest = captor.getValue();
        assertEquals(browserSwitchRequest.getRequestCode(), 123);
        assertEquals(browserSwitchRequest.getUri(), uri);
        assertSame(browserSwitchRequest.getMetadata(), metadata);
    }

    @Test
    public void start_whenRequestCodeIsIntegerMinValue_throwsError() {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, uri)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(Integer.MIN_VALUE)
                .url(uri)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);
        try {
            sut.start(plainActivity, options);
            fail("should fail");
        } catch (BrowserSwitchException e) {
            assertEquals(e.getMessage(), "Request code cannot be Integer.MIN_VALUE");
        }
    }

    @Test
    public void start_whenDeviceIsNotConfiguredForDeepLinking_throwsError() {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, uri)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(false);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(uri)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);

        try {
            sut.start(plainActivity, options);
            fail("should fail");
        } catch (BrowserSwitchException e) {
            assertEquals("The return url scheme was not set up, incorrectly set up, or more than one " +
                    "Activity on this device defines the same url scheme in it's Android Manifest. " +
                    "See https://github.com/braintree/browser-switch-android for more information on " +
                    "setting up a return url scheme.", e.getMessage());
        }
    }

    @Test
    public void start_whenNoActivityFoundCanOpenURL_throwsError() {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, uri)).thenReturn(false);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        when(uri.toString()).thenReturn("https://example.com/");

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(uri)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);
        try {
            sut.start(plainActivity, options);
            fail("should fail");
        } catch (BrowserSwitchException e) {
            assertEquals("No installed activities can open this URL: https://example.com/", e.getMessage());
        }
    }

    @Test
    public void start_whenNoReturnUrlSchemeSet_throwsError() {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, uri)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        when(uri.toString()).thenReturn("https://example.com/");

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .returnUrlScheme(null)
                .url(uri)
                .metadata(metadata);
        try {
            sut.start(plainActivity, options);
            fail("should fail");
        } catch (BrowserSwitchException e) {
            assertEquals("A returnUrlScheme is required.", e.getMessage());
        }
    }

    @Test
    public void deliverResult_whenRequestIsSuccessful_clearsResultStoreAndNotifiesResultOK() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUri = mock(Uri.class);
        Intent deepLinkIntent = mock(Intent.class);
        when(deepLinkIntent.getData()).thenReturn(deepLinkUri);

        when(plainActivity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
            new BrowserSwitchRequest(123, uri, requestMetadata);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);
        sut.deliverResult(plainActivity, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(captor.capture());

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(uri, result.getRequestUrl());
        assertEquals(BrowserSwitchResult.STATUS_SUCCESS, result.getStatus());
        assertSame(requestMetadata, result.getRequestMetadata());
        assertSame(deepLinkUri, result.getDeepLinkUrl());

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsPending_clearsResultStoreAndNotifiesResultCANCELLED() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, uri, requestMetadata);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);
        sut.deliverResult(plainActivity, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(captor.capture());

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(uri, result.getRequestUrl());
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);
        assertNull(result.getDeepLinkUrl());

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsNull_doesNothing() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);
        sut.deliverResult(plainActivity, browserSwitchListener);

        verify(browserSwitchListener, never()).onBrowserSwitchResult(any());
        verify(persistentStore, never()).clearActiveRequest(plainActivity);
    }

    @Test
    public void deliverResult_whenActivityIsNotABrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Activity must implement BrowserSwitchListener.");

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder);
        sut.deliverResult(plainActivity);
    }

    @Test
    public void convenience_deliverResultWithActivityListener_forwardsInvocationToPrimaryDeliverResultMethod() {
        BrowserSwitchClient sut = spy(new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsIntentBuilder));
        doNothing().when(sut).deliverResult(any(FragmentActivity.class), any(BrowserSwitchListener.class));

        sut.deliverResult(activityAndListener);
        verify(sut).deliverResult(activityAndListener, activityAndListener);
    }
}
