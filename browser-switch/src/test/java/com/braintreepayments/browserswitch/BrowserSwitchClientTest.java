package com.braintreepayments.browserswitch;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

public class BrowserSwitchClientTest {

    static abstract class ActivityListener extends FragmentActivity implements BrowserSwitchListener {}

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private ActivityFinder activityFinder;
    private BrowserSwitchConfig browserSwitchConfig;
    private BrowserSwitchPersistentStore persistentStore;

    private Uri uri;

    private FragmentActivity plainActivity;

    private ActivityListener activityAndListener;

    private Context applicationContext;
    private BrowserSwitchListener browserSwitchListener;

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
        browserSwitchListener = mock(BrowserSwitchListener.class);

        returnUrlScheme = "sample-url-scheme";
    }

    //region test startWithOptions

    @Test
    public void startWithOptionsAndExplicitListener_withUri_createsBrowserSwitchIntentAndInitiatesBrowserSwitch() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        Intent queryIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentForBrowserSwitchActivityQuery(returnUrlScheme)).thenReturn(queryIntent);

        Intent browserSwitchIntent = mock(Intent.class);
        when(browserSwitchConfig.createIntentToLaunchUriInBrowser(applicationContext, uri)).thenReturn(browserSwitchIntent);

        when(activityFinder.canResolveActivityForIntent(applicationContext, queryIntent)).thenReturn(true);
        when(activityFinder.canResolveActivityForIntent(applicationContext, browserSwitchIntent)).thenReturn(true);

        when(browserSwitchIntent.getData()).thenReturn(uri);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(uri)
                .metadata(metadata);
        sut.start(options, plainActivity, browserSwitchListener);

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
    public void startWithOptionsAndExplicitListener_whenRequestCodeIsIntegerMinValue_notifiesListenerOfError() {
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

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(Integer.MIN_VALUE)
                .url(uri)
                .metadata(metadata);
        sut.start(options, plainActivity, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
            ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(anyInt(), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_ERROR);
        assertEquals(result.getErrorMessage(), "Request code cannot be Integer.MIN_VALUE");
    }

    @Test
    public void startWithOptions_whenIsNotConfiguredForBrowserSwitch_notifiesListenerOfError() {
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

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(uri)
                .metadata(metadata);
        sut.start(options, plainActivity, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(eq(123), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_ERROR);
        assertEquals("The return url scheme was not set up, incorrectly set up, or more than one " +
                "Activity on this device defines the same url scheme in it's Android Manifest. " +
                "See https://github.com/braintree/browser-switch-android for more information on " +
                "setting up a return url scheme.", result.getErrorMessage());
    }

    @Test
    public void startWithOptions_whenNoActivityFoundCanOpenURL_notifiesListenerOfError() {
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

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(uri)
                .metadata(metadata);
        sut.start(options, plainActivity, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor = ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(eq(123), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_ERROR);
        assertEquals("No installed activities can open this URL: https://example.com/", result.getErrorMessage());
    }

    @Test
    public void startWithOptions_whenActivityIsNotBrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Activity must implement BrowserSwitchListener.");

        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .requestCode(123);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(browserSwitchOptions, plainActivity);
    }

    //endregion

    //region test convenience start methods

    @Test
    public void convenience_startWithOptionsAndActivityListener_forwardsInvocationToPrimaryStartWithOptionsMethod() {
        sut = spy(BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme));
        doNothing().when(sut).start(any(BrowserSwitchOptions.class), any(FragmentActivity.class), any(BrowserSwitchListener.class));

        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions();
        sut.start(browserSwitchOptions, activityAndListener);
        verify(sut).start(browserSwitchOptions, activityAndListener, activityAndListener);
    }

    //endregion

    //region test deliverResult

    @Test
    public void deliverResult_whenRequestIsSuccessful_clearsResultStoreAndNotifiesResultOK() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
            new BrowserSwitchRequest(123, uri, BrowserSwitchRequest.SUCCESS, requestMetadata);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(plainActivity, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(eq(123), captor.capture(), same(uri));

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_OK);
        assertNull(result.getErrorMessage());
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

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(plainActivity, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(eq(123), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_CANCELED);
        assertNull(result.getErrorMessage());
        assertSame(result.getRequestMetadata(), requestMetadata);

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsNull_doesNothing() {
        when(plainActivity.getApplicationContext()).thenReturn(applicationContext);

        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(plainActivity, browserSwitchListener);

        verify(browserSwitchListener, never()).onBrowserSwitchResult(anyInt(), any(), any());
        verify(persistentStore, never()).clearActiveRequest(plainActivity);
    }

    @Test
    public void deliverResult_whenActivityIsNotABrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Activity must implement BrowserSwitchListener.");

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(plainActivity);
    }

    //endregion

    //region test convenience deliverResult methods

    @Test
    public void convenience_deliverResultWithActivityListener_forwardsInvocationToPrimaryDeliverResultMethod() {
        sut = spy(BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme));
        doNothing().when(sut).deliverResult(any(FragmentActivity.class), any(BrowserSwitchListener.class));

        sut.deliverResult(activityAndListener);
        verify(sut).deliverResult(activityAndListener, activityAndListener);
    }

    //endregion

    //region test captureResult

    @Test
    public void captureResult_whenActiveRequestExistsAndIntentHasData_updatesActiveRequestToSuccessState() {
        Context context = mock(Context.class);
        BrowserSwitchRequest request = mock(BrowserSwitchRequest.class);

        when(persistentStore.getActiveRequest(context)).thenReturn(request);

        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(uri);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.captureResult(intent, context);

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

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.captureResult(intent, context);

        verify(persistentStore, never()).putActiveRequest(any(), any());
    }

    @Test
    public void captureResult_whenIntentHasNoData_doesNothing() {
        Context context = mock(Context.class);
        BrowserSwitchRequest request = mock(BrowserSwitchRequest.class);
        when(persistentStore.getActiveRequest(context)).thenReturn(request);

        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(null);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.captureResult(intent, context);

        verify(request, never()).setUri(any());
        verify(request, never()).setState(any());
        verify(persistentStore, never()).putActiveRequest(any(), any());
    }

    @Test
    public void captureResult_whenIntentIsNull_doesNothing() {
        Context context = mock(Context.class);
        BrowserSwitchRequest request = mock(BrowserSwitchRequest.class);
        when(persistentStore.getActiveRequest(context)).thenReturn(request);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.captureResult(null, context);

        verify(request, never()).setUri(any());
        verify(request, never()).setState(any());
        verify(persistentStore, never()).putActiveRequest(any(), any());
    }

    //endregion
}
