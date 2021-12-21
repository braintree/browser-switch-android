package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchObserverUnitTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private BrowserSwitchListenerFinder listenerFinder;
    private BrowserSwitchPersistentStore persistentStore;

    private Uri browserSwitchDestinationUrl;
    private Context applicationContext;

    private FragmentActivity activity;

    @Before
    public void beforeEach() {
        browserSwitchDestinationUrl = Uri.parse("https://example.com/browser_switch_destination");

        listenerFinder = mock(BrowserSwitchListenerFinder.class);
        persistentStore = mock(BrowserSwitchPersistentStore.class);

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

        BrowserSwitchListener listener = mock(BrowserSwitchListener.class);
        when(listenerFinder.findActiveListeners(activity)).thenReturn(Collections.singletonList(listener));

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore, listenerFinder);
        sut.onActivityResumed(activity);

        ArgumentCaptor<BrowserSwitchResult> captor =
            ArgumentCaptor.forClass(BrowserSwitchResult.class);

        verify(listener).onBrowserSwitchResult(captor.capture());
        BrowserSwitchResult result = captor.getValue();

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

        BrowserSwitchListener listener = mock(BrowserSwitchListener.class);
        when(listenerFinder.findActiveListeners(activity)).thenReturn(Collections.singletonList(listener));

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore, listenerFinder);
        sut.onActivityResumed(activity);

        ArgumentCaptor<BrowserSwitchResult> resultCaptor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);

        verify(listener).onBrowserSwitchResult(resultCaptor.capture());
        BrowserSwitchResult result = resultCaptor.getValue();

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(browserSwitchDestinationUrl, result.getRequestUrl());
        assertEquals(result.getStatus(), BrowserSwitchStatus.CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);
        assertNull(result.getDeepLinkUrl());

        ArgumentCaptor<BrowserSwitchRequest> requestCaptor = ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(requestCaptor.capture(), same(activity));

        BrowserSwitchRequest updatedRequest = requestCaptor.getValue();
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

        BrowserSwitchListener listener = mock(BrowserSwitchListener.class);
        when(listenerFinder.findActiveListeners(activity)).thenReturn(Collections.singletonList(listener));

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore, listenerFinder);
        sut.onActivityResumed(activity);

        verify(listener, never()).onBrowserSwitchResult(any(BrowserSwitchResult.class));
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

        BrowserSwitchListener listener = mock(BrowserSwitchListener.class);
        when(listenerFinder.findActiveListeners(activity)).thenReturn(Collections.singletonList(listener));

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore, listenerFinder);
        sut.onActivityResumed(activity);

        ArgumentCaptor<BrowserSwitchResult> resultCaptor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);

        verify(listener).onBrowserSwitchResult(resultCaptor.capture());
        BrowserSwitchResult result = resultCaptor.getValue();

        assertNotNull(result);
        assertEquals(123, result.getRequestCode());
        assertSame(browserSwitchDestinationUrl, result.getRequestUrl());
        assertEquals(result.getStatus(), BrowserSwitchStatus.CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);
        assertNull(result.getDeepLinkUrl());

        ArgumentCaptor<BrowserSwitchRequest> requestCaptor = ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(requestCaptor.capture(), same(activity));

        BrowserSwitchRequest updatedRequest = requestCaptor.getValue();
        assertSame(request, updatedRequest);
        assertFalse(updatedRequest.getShouldNotifyCancellation());
    }

    @Test
    public void onActivityResumed_whenDeepLinkUrlDoesNotExistAndShouldNotNotifyCancellation_doesNothing() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(activity.getIntent()).thenReturn(new Intent());

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "return-url-scheme", false);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        BrowserSwitchListener listener = mock(BrowserSwitchListener.class);
        when(listenerFinder.findActiveListeners(activity)).thenReturn(Collections.singletonList(listener));

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore, listenerFinder);
        sut.onActivityResumed(activity);

        verify(listener, never()).onBrowserSwitchResult(any(BrowserSwitchResult.class));
        verify(persistentStore, never()).putActiveRequest(any(BrowserSwitchRequest.class), any(FragmentActivity.class));
    }

    @Test
    public void onActivityResumed_whenRequestIsNull_doesNothing() {
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);

        BrowserSwitchListener listener = mock(BrowserSwitchListener.class);
        when(listenerFinder.findActiveListeners(activity)).thenReturn(Collections.singletonList(listener));

        BrowserSwitchObserver sut = new BrowserSwitchObserver(persistentStore, listenerFinder);
        sut.onActivityResumed(activity);

        verify(listener, never()).onBrowserSwitchResult(any(BrowserSwitchResult.class));
        verify(persistentStore, never()).clearActiveRequest(activity);
    }
}
