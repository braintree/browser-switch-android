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
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrowserSwitchClientUnitTest {

    static abstract class ActivityListener extends FragmentActivity implements BrowserSwitchCallback {}

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private ActivityFinder activityFinder;
    private BrowserSwitchConfig browserSwitchConfig;
    private BrowserSwitchPersistentStore persistentStore;

    private Uri uri;

    private FragmentActivity plainActivity;

    private ActivityListener activityAndListener;

    private Context applicationContext;
    private BrowserSwitchCallback browserSwitchCallback;

    private BrowserSwitchClient sut;

    private String returnUrlScheme;

    @Before
    public void beforeEach() {
        activityFinder = mock(ActivityFinder.class);
        browserSwitchConfig = mock(BrowserSwitchConfig.class);
        persistentStore = mock(BrowserSwitchPersistentStore.class);

        uri = mock(Uri.class);

        plainActivity = mock(FragmentActivity.class);
        activityAndListener = mock(ActivityListener.class);

        applicationContext = mock(Context.class);
        browserSwitchCallback = mock(BrowserSwitchCallback.class);

        returnUrlScheme = "sample-url-scheme";
    }

    @Test
    public void startWithOptions_createsBrowserSwitchIntentAndInitiatesBrowserSwitch() throws BrowserSwitchException {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        Intent queryIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentForBrowserSwitchActivityQuery(returnUrlScheme)).thenReturn(queryIntent);

        Intent browserSwitchIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentToLaunchUriInBrowser(applicationContext, uri)).thenReturn(browserSwitchIntent);

        when(activityFinder.canResolveActivityForIntent(applicationContext, queryIntent)).thenReturn(true);
        when(activityFinder.canResolveActivityForIntent(applicationContext, browserSwitchIntent)).thenReturn(true);

        when(browserSwitchIntent.getData()).thenReturn(uri);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(uri)
                .returnUrlScheme(returnUrlScheme)
                .metadata(metadata);
        sut.start(plainActivity, options);

        verify(applicationContext).startActivity(browserSwitchIntent);

        ArgumentCaptor<BrowserSwitchRequest> captor =
                ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(captor.capture(), same(applicationContext));

        BrowserSwitchRequest browserSwitchRequest = captor.getValue();
        assertEquals(browserSwitchRequest.getRequestCode(), 123);
        assertEquals(browserSwitchRequest.getUri(), uri);
        assertEquals(browserSwitchRequest.getState(), BrowserSwitchRequest.PENDING);
        assertSame(browserSwitchRequest.getMetadata(), metadata);
    }

    @Test
    public void startWithOptionsAndExplicitListener_whenRequestCodeIsIntegerMinValue_throwsError() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        Intent queryIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentForBrowserSwitchActivityQuery(returnUrlScheme))
                .thenReturn(queryIntent);

        Intent browserSwitchIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentToLaunchUriInBrowser(applicationContext, uri))
                .thenReturn(browserSwitchIntent);

        when(activityFinder.canResolveActivityForIntent(applicationContext, queryIntent))
                .thenReturn(true);
        when(activityFinder.canResolveActivityForIntent(applicationContext, browserSwitchIntent))
                .thenReturn(true);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);

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
    public void startWithOptions_whenIsNotConfiguredForBrowserSwitch_throwsError() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        Intent queryIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentForBrowserSwitchActivityQuery(returnUrlScheme))
                .thenReturn(queryIntent);

        Intent browserSwitchIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentToLaunchUriInBrowser(applicationContext, uri))
                .thenReturn(browserSwitchIntent);

        when(activityFinder.canResolveActivityForIntent(applicationContext, queryIntent))
                .thenReturn(false);
        when(activityFinder.canResolveActivityForIntent(applicationContext, browserSwitchIntent))
                .thenReturn(true);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);

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
    public void startWithOptions_whenNoActivityFoundCanOpenURL_throwsError() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        Intent queryIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentForBrowserSwitchActivityQuery(returnUrlScheme))
                .thenReturn(queryIntent);

        Intent browserSwitchIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentToLaunchUriInBrowser(applicationContext, uri))
                .thenReturn(browserSwitchIntent);

        when(activityFinder.canResolveActivityForIntent(applicationContext, queryIntent))
                .thenReturn(true);
        when(activityFinder.canResolveActivityForIntent(applicationContext, browserSwitchIntent))
                .thenReturn(false);

        when(browserSwitchIntent.getData()).thenReturn(uri);
        when(uri.toString()).thenReturn("https://example.com/");

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);

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
    public void deliverResult_whenRequestIsSuccessful_clearsResultStoreAndNotifiesResultOK() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
            new BrowserSwitchRequest(123, uri, BrowserSwitchRequest.SUCCESS, requestMetadata);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.deliverResult(plainActivity, browserSwitchCallback);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchCallback).onResult(eq(123), captor.capture(), same(uri));

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_OK);
        assertSame(result.getRequestMetadata(), requestMetadata);

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsPending_clearsResultStoreAndNotifiesResultCANCELLED() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, uri, BrowserSwitchRequest.PENDING, requestMetadata);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.deliverResult(plainActivity, browserSwitchCallback);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchCallback).onResult(eq(123), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_CANCELED);
        assertSame(result.getRequestMetadata(), requestMetadata);

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsNull_doesNothing() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);
        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.deliverResult(plainActivity, browserSwitchCallback);

        verify(browserSwitchCallback, never()).onResult(anyInt(), any(), any());
        verify(persistentStore, never()).clearActiveRequest(plainActivity);
    }

    @Test
    public void deliverResult_whenActivityIsNotABrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Activity must implement BrowserSwitchListener.");

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.deliverResult(plainActivity);
    }

    @Test
    public void convenience_deliverResultWithActivityListener_forwardsInvocationToPrimaryDeliverResultMethod() {
        sut = spy(new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore));
        doNothing().when(sut).deliverResult(any(FragmentActivity.class), any(BrowserSwitchCallback.class));

        sut.deliverResult(activityAndListener);
        verify(sut).deliverResult(activityAndListener, activityAndListener);
    }

    @Test
    public void captureResult_whenActiveRequestExistsAndIntentHasData_updatesActiveRequestToSuccessState() {
        Context context = mock(Context.class);
        BrowserSwitchRequest request = mock(BrowserSwitchRequest.class);

        when(persistentStore.getActiveRequest(context)).thenReturn(request);

        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(uri);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.captureResult(context, intent);

        InOrder inOrder = Mockito.inOrder(request, persistentStore);

        inOrder.verify(request).setUri(uri);
        inOrder.verify(request).setState(BrowserSwitchRequest.SUCCESS);
        inOrder.verify(persistentStore).putActiveRequest(request, context);
    }

    @Test
    public void captureResult_whenNoActiveRequestExists_doesNothing() {
        Context context = mock(Context.class);
        when(persistentStore.getActiveRequest(context)).thenReturn(null);

        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(uri);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.captureResult(context, intent);

        verify(persistentStore, never()).putActiveRequest(any(), any());
    }

    @Test
    public void captureResult_whenIntentHasNoData_doesNothing() {
        Context context = mock(Context.class);
        BrowserSwitchRequest request = mock(BrowserSwitchRequest.class);
        when(persistentStore.getActiveRequest(context)).thenReturn(request);

        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(null);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.captureResult(context, intent);

        verify(request, never()).setUri(any());
        verify(request, never()).setState(any());
        verify(persistentStore, never()).putActiveRequest(any(), any());
    }

    @Test
    public void captureResult_whenIntentIsNull_doesNothing() {
        Context context = mock(Context.class);
        BrowserSwitchRequest request = mock(BrowserSwitchRequest.class);
        when(persistentStore.getActiveRequest(context)).thenReturn(request);

        sut = new BrowserSwitchClient(browserSwitchConfig, activityFinder, persistentStore);
        sut.captureResult(context, null);

        verify(request, never()).setUri(any());
        verify(request, never()).setState(any());
        verify(persistentStore, never()).putActiveRequest(any(), any());
    }
}
