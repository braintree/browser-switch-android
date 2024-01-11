package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchClientUnitTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private BrowserSwitchPersistentStore persistentStore;
    private BrowserSwitchInspector browserSwitchInspector;

    private ChromeCustomTabsInternalClient customTabsInternalClient;

    private Uri browserSwitchDestinationUrl;
    private Context applicationContext;

    private ActivityController<FragmentActivity> activityController;
    private FragmentActivity activity;

    private ActivityController<ComponentActivity> componentActivityController;
    private ComponentActivity componentActivity;
    @Before
    public void beforeEach() {
        persistentStore = mock(BrowserSwitchPersistentStore.class);

        browserSwitchInspector = mock(BrowserSwitchInspector.class);
        customTabsInternalClient = mock(ChromeCustomTabsInternalClient.class);

        browserSwitchDestinationUrl = Uri.parse("https://example.com/browser_switch_destination");

        activityController = Robolectric.buildActivity(FragmentActivity.class).setup();
        activity = spy(activityController.get());

        componentActivityController = Robolectric.buildActivity(ComponentActivity.class).setup();
        componentActivity = spy(componentActivityController.get());

        applicationContext = activity.getApplicationContext();
    }

    @Test
    public void start_whenActivityIsFinishing_throwsException() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        when(componentActivity.isFinishing()).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);

        BrowserSwitchPendingRequest request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchPendingRequest.Failure);
        assertEquals(((BrowserSwitchPendingRequest.Failure) request).getError().getMessage(), "Unable to start browser switch while host Activity is finishing.");
    }

    @Test
    public void start_whenSuccessful_returnsBrowserSwitchRequest() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);
        BrowserSwitchPendingRequest browserSwitchPendingRequest = sut.start(componentActivity, options);

        verify(customTabsInternalClient).launchUrl(componentActivity, browserSwitchDestinationUrl, false);

        assertNotNull(browserSwitchPendingRequest);
        assertTrue(browserSwitchPendingRequest instanceof BrowserSwitchPendingRequest.Started);

        BrowserSwitchRequest browserSwitchRequest = ((BrowserSwitchPendingRequest.Started) browserSwitchPendingRequest).getBrowserSwitchRequest();
        assertEquals(browserSwitchRequest.getRequestCode(), 123);
        assertEquals(browserSwitchRequest.getUrl(), browserSwitchDestinationUrl);
        assertSame(browserSwitchRequest.getMetadata(), metadata);
        assertTrue(browserSwitchRequest.getShouldNotifyCancellation());
    }

    @Test
    public void start_whenNoBrowserAvailable_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);
        doThrow(new ActivityNotFoundException()).when(customTabsInternalClient).launchUrl(any(Context.class), any(Uri.class), eq(false));

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);
        BrowserSwitchPendingRequest request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchPendingRequest.Failure);
        assertEquals(((BrowserSwitchPendingRequest.Failure) request).getError().getMessage(), "Unable to start browser switch without a web browser.");
    }
    @Test
    public void start_whenRequestCodeIsIntegerMinValue_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(Integer.MIN_VALUE)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);
        BrowserSwitchPendingRequest request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchPendingRequest.Failure);
        assertEquals(((BrowserSwitchPendingRequest.Failure) request).getError().getMessage(), "Request code cannot be Integer.MIN_VALUE");
    }

    @Test
    public void start_whenDeviceIsNotConfiguredForDeepLinking_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(false);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);

        BrowserSwitchPendingRequest request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchPendingRequest.Failure);
        assertEquals("The return url scheme was not set up, incorrectly set up, or more than one " +
                "Activity on this device defines the same url scheme in it's Android Manifest. " +
                "See https://github.com/braintree/browser-switch-android for more information on " +
                "setting up a return url scheme.", ((BrowserSwitchPendingRequest.Failure) request).getError().getMessage());
    }

    @Test
    public void start_whenNoReturnUrlSchemeSet_throwsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .returnUrlScheme(null)
                .url(browserSwitchDestinationUrl)
                .metadata(metadata);
        BrowserSwitchPendingRequest request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchPendingRequest.Failure);
        assertEquals("A returnUrlScheme is required.", ((BrowserSwitchPendingRequest.Failure) request).getError().getMessage());
    }

    @Test
    public void deliverResult_whenDeepLinkUrlExistsAndReturnUrlSchemeMatchesAndNoPendingRequest_returnsNull() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = Uri.parse("return-url-scheme://test");
        Intent deepLinkIntent = new Intent().setData(deepLinkUrl);
        when(activity.getIntent()).thenReturn(deepLinkIntent);

        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNull(result);
    }

    @Test
    public void deliverResult_whenDeepLinkUrlExists_AndReturnUrlSchemeDoesNotMatch_AndShouldNotifyCancellation_notifiesResultCANCELED_AndSetsRequestShouldNotifyCancellationToFalse() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = Uri.parse("another-return-url-scheme://test");
        Intent deepLinkIntent = new Intent().setData(deepLinkUrl);
        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", true);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(browserSwitchDestinationUrl, result.getRequestUrl());
        assertEquals(result.getStatus(), BrowserSwitchStatus.CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);
        assertNull(result.getDeepLinkUrl());

        ArgumentCaptor<BrowserSwitchRequest> captor = ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(captor.capture(), same(activity));

        BrowserSwitchRequest updatedRequest = captor.getValue();
        assertSame(request, updatedRequest);
        assertFalse(updatedRequest.getShouldNotifyCancellation());
    }

    @Test
    public void deliverResult_whenDeepLinkUrlExistsAndReturnUrlSchemeDoesNotMatchAndShouldNotNotifyCancellation_returnsNull() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = Uri.parse("another-return-url-scheme://test");
        Intent deepLinkIntent = new Intent().setData(deepLinkUrl);
        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNull(result);
        verify(persistentStore, never()).putActiveRequest(any(BrowserSwitchRequest.class), any(FragmentActivity.class));
    }

    @Test
    public void deliverResult_whenDeepLinkUrlDoesNotExistAndShouldNotifyCancellation_notifiesResultCANCELEDAndSetsRequestShouldNotifyCancellationToFalse() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(activity.getIntent()).thenReturn(new Intent());

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", true);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(browserSwitchDestinationUrl, result.getRequestUrl());
        assertEquals(result.getStatus(), BrowserSwitchStatus.CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);
        assertNull(result.getDeepLinkUrl());

        ArgumentCaptor<BrowserSwitchRequest> captor = ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(captor.capture(), same(activity));

        BrowserSwitchRequest updatedRequest = captor.getValue();
        assertSame(request, updatedRequest);
        assertFalse(updatedRequest.getShouldNotifyCancellation());
    }

    @Test
    public void deliverResult_whenDeepLinkUrlDoesNotExistAndShouldNotNotifyCancellation_returnsNull() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(activity.getIntent()).thenReturn(new Intent());

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult result = sut.deliverResult(activity);

        assertNull(result);
        verify(persistentStore, never()).putActiveRequest(any(BrowserSwitchRequest.class), any(FragmentActivity.class));
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

    @Test
    public void captureResult_whenDeepLinkUrlExistsAndReturnUrlSchemeMatches_storesSuccessResultInPersistentStore() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = Uri.parse("return-url-scheme://test");
        Intent deepLinkIntent = new Intent().setData(deepLinkUrl);
        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        sut.captureResult(activity);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(persistentStore).putActiveResult(captor.capture(), same(applicationContext));

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(browserSwitchDestinationUrl, result.getRequestUrl());
        assertEquals(BrowserSwitchStatus.SUCCESS, result.getStatus());
        assertSame(requestMetadata, result.getRequestMetadata());
        assertSame(deepLinkUrl, result.getDeepLinkUrl());
    }

    @Test
    public void captureResult_whenRequestIsNull_doesNothing() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        sut.captureResult(activity);

        verify(persistentStore, never()).putActiveResult(any(BrowserSwitchResult.class), any(Context.class));
    }

    @Test
    public void captureResult_whenIntentIsNull_doesNothing() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(activity.getIntent()).thenReturn(null);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        sut.captureResult(activity);

        verify(persistentStore, never()).putActiveResult(any(BrowserSwitchResult.class), any(Context.class));
    }

    @Test
    public void deliverResultFromCache_forwardsCachedResultFromBrowserSwitchPersistentStorageAndRemovesAllItemsFromPersistentStorage() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        BrowserSwitchResult cachedResult = new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, request, Uri.parse("example://success/url"));
        when(persistentStore.getActiveResult(same(applicationContext))).thenReturn(cachedResult);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        BrowserSwitchResult actualResult = sut.deliverResultFromCache(activity);

        assertSame(cachedResult, actualResult);
        verify(persistentStore).removeAll(applicationContext);
    }

    @Test
    public void parseResult_whenActiveRequestMatchesRequestCodeAndDeepLinkResultURLScheme_returnsBrowserSwitchSuccessResult() {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
            new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "fake-url-scheme", false);
        when(persistentStore.getActiveRequest(same(applicationContext))).thenReturn(request);

        Uri deepLinkUrl = Uri.parse("fake-url-scheme://success");
        Intent intent = new Intent(Intent.ACTION_VIEW, deepLinkUrl);
        BrowserSwitchResult browserSwitchResult = sut.parseResult(applicationContext, 123, intent);

        assertNotNull(browserSwitchResult);
        assertEquals(BrowserSwitchStatus.SUCCESS, browserSwitchResult.getStatus());
        assertEquals(deepLinkUrl, browserSwitchResult.getDeepLinkUrl());
    }

    @Test
    public void parseResult_whenActiveRequestMatchesRequestCodeAndDeepLinkResultURLSchemeDoesntMatch_returnsNull() {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "fake-url-scheme", false);
        when(persistentStore.getActiveRequest(same(applicationContext))).thenReturn(request);

        Uri deepLinkUrl = Uri.parse("a-different-url-scheme://success");
        Intent intent = new Intent(Intent.ACTION_VIEW, deepLinkUrl);
        BrowserSwitchResult browserSwitchResult = sut.parseResult(applicationContext, 123, intent);

        assertNull(browserSwitchResult);
    }

    @Test
    public void parseResult_whenActiveRequestDoesntMatchRequestCode_returnsNull() {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(456, browserSwitchDestinationUrl, requestMetadata, "fake-url-scheme", false);
        when(persistentStore.getActiveRequest(same(applicationContext))).thenReturn(request);

        Uri deepLinkUrl = Uri.parse("fake-url-scheme://success");
        Intent intent = new Intent(Intent.ACTION_VIEW, deepLinkUrl);
        BrowserSwitchResult browserSwitchResult = sut.parseResult(applicationContext, 123, intent);

        assertNull(browserSwitchResult);
    }

    @Test
    public void parseResult_whenNoActiveRequestExists_returnsNull() {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);
        when(persistentStore.getActiveRequest(same(applicationContext))).thenReturn(null);

        Uri deepLinkUrl = Uri.parse("fake-url-scheme://success");
        Intent intent = new Intent(Intent.ACTION_VIEW, deepLinkUrl);
        BrowserSwitchResult browserSwitchResult = sut.parseResult(applicationContext, 123, intent);

        assertNull(browserSwitchResult);
    }

    @Test
    public void parseResult_whenIntentIsNull_returnsNull() {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        BrowserSwitchResult browserSwitchResult = sut.parseResult(applicationContext, 123, null);
        assertNull(browserSwitchResult);
    }

    @Test
    public void clearActiveRequests_forwardsInvocationToPersistantStore() {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, persistentStore, customTabsInternalClient);

        sut.clearActiveRequests(applicationContext);
        verify(persistentStore).clearActiveRequest(applicationContext);
    }
}
