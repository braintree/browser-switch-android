package com.braintreepayments.api;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchObserverUnitTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private BrowserSwitchPersistentStore persistentStore;

    private Uri browserSwitchDestinationUrl;
    private Context applicationContext;

    private FragmentActivity activity;

    @Before
    public void beforeEach() {
        persistentStore = mock(BrowserSwitchPersistentStore.class);
        browserSwitchDestinationUrl = Uri.parse("https://example.com/browser_switch_destination");

        activity = mock(FragmentActivity.class);
        applicationContext = mock(Context.class);

        when(activity.getApplicationContext()).thenReturn(applicationContext);
    }

    @Test
    public void onActivityResumed_whenDeepLinkUrlExistsAndReturnUrlSchemeMatches_clearsResultStoreAndNotifiesResultSUCCESS() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = Uri.parse("return-url-scheme://test");
        Intent deepLinkIntent = new Intent().setData(deepLinkUrl);
        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore);
        BrowserSwitchResult result = sut.onActivityResumed(activity);

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(browserSwitchDestinationUrl, result.getRequestUrl());
        assertEquals(BrowserSwitchStatus.SUCCESS, result.getStatus());
        assertSame(requestMetadata, result.getRequestMetadata());
        assertSame(deepLinkUrl, result.getDeepLinkUrl());

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void onActivityResumed_whenDeepLinkUrlExists_AndReturnUrlSchemeDoesNotMatch_AndShouldNotifyCancellation_notifiesResultCANCELED_AndSetsRequestShouldNotifyCancellationToFalse() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = Uri.parse("another-return-url-scheme://test");
        Intent deepLinkIntent = new Intent().setData(deepLinkUrl);
        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", true);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore);
        BrowserSwitchResult result = sut.onActivityResumed(activity);

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
    public void onActivityResumed_whenDeepLinkUrlExistsAndReturnUrlSchemeDoesNotMatchAndShouldNotNotifyCancellation_returnsNull() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        Uri deepLinkUrl = Uri.parse("another-return-url-scheme://test");
        Intent deepLinkIntent = new Intent().setData(deepLinkUrl);
        when(activity.getIntent()).thenReturn(deepLinkIntent);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore);
        BrowserSwitchResult result = sut.onActivityResumed(activity);

        assertNull(result);
        verify(persistentStore, never()).putActiveRequest(any(BrowserSwitchRequest.class), any(FragmentActivity.class));
    }

    @Test
    public void onActivityResumed_whenDeepLinkUrlDoesNotExistAndShouldNotifyCancellation_notifiesResultCANCELEDAndSetsRequestShouldNotifyCancellationToFalse() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(activity.getIntent()).thenReturn(new Intent());

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", true);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore);
        BrowserSwitchResult result = sut.onActivityResumed(activity);

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
    public void onActivityResumed_whenDeepLinkUrlDoesNotExistAndShouldNotNotifyCancellation_returnsNull() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(activity.getIntent()).thenReturn(new Intent());

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore);
        BrowserSwitchResult result = sut.onActivityResumed(activity);

        assertNull(result);
        verify(persistentStore, never()).putActiveRequest(any(BrowserSwitchRequest.class), any(FragmentActivity.class));
    }

    @Test
    public void onActivityResumed_whenRequestIsNull_doesNothing() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore);
        BrowserSwitchResult result = sut.onActivityResumed(activity);

        assertNull(result);
        verify(persistentStore, never()).clearActiveRequest(activity);
    }
}
