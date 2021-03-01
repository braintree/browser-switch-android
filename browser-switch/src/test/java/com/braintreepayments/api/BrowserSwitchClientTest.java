package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrowserSwitchClientTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private BrowserSwitchPersistentStore persistentStore;
    private BrowserSwitchInspector browserSwitchInspector;

    private ChromeCustomTabsInternalClient customTabsInternalClient;

    private Uri url;
    private Context applicationContext;

    private FragmentActivity activity;
    private String returnUrlScheme;

    @Before
    public void beforeEach() {
        persistentStore = mock(BrowserSwitchPersistentStore.class);

        browserSwitchInspector = mock(BrowserSwitchInspector.class);
        customTabsInternalClient = mock(ChromeCustomTabsInternalClient.class);

        url = mock(Uri.class);

        activity = mock(FragmentActivity.class);
        applicationContext = mock(Context.class);

        returnUrlScheme = "sample-url-scheme";

        when(activity.getApplicationContext()).thenReturn(applicationContext);
    }

    @Test
    public void start_createsBrowserSwitchIntentAndInitiatesBrowserSwitch() throws BrowserSwitchException {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, url)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(url)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);
        sut.start(activity, options);

        verify(customTabsInternalClient).launchUrl(activity, url);

        ArgumentCaptor<BrowserSwitchRequest> captor =
                ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(captor.capture(), same(applicationContext));

        BrowserSwitchRequest browserSwitchRequest = captor.getValue();
        assertEquals(browserSwitchRequest.getRequestCode(), 123);
        assertEquals(browserSwitchRequest.getUrl(), url);
        assertSame(browserSwitchRequest.getMetadata(), metadata);
    }

    @Test
    public void start_whenRequestCodeIsIntegerMinValue_throwsError() {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, url)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(Integer.MIN_VALUE)
                .url(url)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);
        try {
            sut.start(activity, options);
            fail("should fail");
        } catch (BrowserSwitchException e) {
            assertEquals(e.getMessage(), "Request code cannot be Integer.MIN_VALUE");
        }
    }

    @Test
    public void start_whenDeviceIsNotConfiguredForDeepLinking_throwsError() {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, url)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(false);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(url)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);

        try {
            sut.start(activity, options);
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
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, url)).thenReturn(false);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        when(url.toString()).thenReturn("https://example.com/");

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(url)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);
        try {
            sut.start(activity, options);
            fail("should fail");
        } catch (BrowserSwitchException e) {
            assertEquals("No installed activities can open this URL: https://example.com/", e.getMessage());
        }
    }

    @Test
    public void start_whenNoReturnUrlSchemeSet_throwsError() {
        when(browserSwitchInspector.canDeviceOpenUrl(applicationContext, url)).thenReturn(true);
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, returnUrlScheme)).thenReturn(true);

        when(url.toString()).thenReturn("https://example.com/");

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .returnUrlScheme(null)
                .url(url)
                .metadata(metadata);
        try {
            sut.start(activity, options);
            fail("should fail");
        } catch (BrowserSwitchException e) {
            assertEquals("A returnUrlScheme is required.", e.getMessage());
        }
    }

    @Test
    public void deliverResult_whenDeepLinkUrlExistsAndReturnUrlSchemeMatches_clearsResultStoreAndNotifiesResultSUCCESS() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = mock(Uri.class);
        when(deepLinkUrl.getScheme()).thenReturn("my-return-url-scheme");

        Intent deepLinkIntent = mock(Intent.class);
        when(deepLinkIntent.getData()).thenReturn(deepLinkUrl);

        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, url, requestMetadata, "my-return-url-scheme");
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(url, result.getRequestUrl());
        assertEquals(BrowserSwitchStatus.SUCCESS, result.getStatus());
        assertSame(requestMetadata, result.getRequestMetadata());
        assertSame(deepLinkUrl, result.getDeepLinkUrl());

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenDeepLinkUrlExistsAndReturnUrlSchemeDoesNotMatch_clearsResultStoreAndNotifiesResultCANCELED() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = mock(Uri.class);
        when(deepLinkUrl.getScheme()).thenReturn("another-return-url-scheme");

        Intent deepLinkIntent = mock(Intent.class);
        when(deepLinkIntent.getData()).thenReturn(deepLinkUrl);

        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, url, requestMetadata, "my-return-url-scheme");
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(url, result.getRequestUrl());
        assertEquals(result.getStatus(), BrowserSwitchStatus.CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);
        assertNull(result.getDeepLinkUrl());

        verify(persistentStore).clearActiveRequest(applicationContext);

    }

    @Test
    public void deliverResult_whenDeepLinkUrlDoesNotExist_clearsResultStoreAndNotifiesResultCANCELED() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(activity.getIntent()).thenReturn(mock(Intent.class));

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, url, requestMetadata, "my-return-url-scheme");
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(url, result.getRequestUrl());
        assertEquals(result.getStatus(), BrowserSwitchStatus.CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);
        assertNull(result.getDeepLinkUrl());

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsNull_doesNothing() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNull(result);
        verify(persistentStore, never()).clearActiveRequest(activity);
    }
}
