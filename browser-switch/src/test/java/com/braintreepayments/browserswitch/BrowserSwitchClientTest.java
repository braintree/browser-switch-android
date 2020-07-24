package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrowserSwitchClientTest {

    static abstract class FragmentListener extends Fragment implements BrowserSwitchListener {}

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private ActivityFinder activityFinder;
    private BrowserSwitchConfig browserSwitchConfig;
    private BrowserSwitchPersistentStore persistentStore;

    private Uri uri;

    private FragmentListener fragmentListener;
    private FragmentActivity activity;

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
        activity = mock(FragmentActivity.class);
        fragmentListener = mock(FragmentListener.class);

        applicationContext = mock(Context.class);
        browserSwitchListener = mock(BrowserSwitchListener.class);

        returnUrlScheme = "sample-url-scheme";
    }

    @Test
    public void start_whenAbleToBrowserSwitch_initiatesBrowserSwitch() {
        when(fragmentListener.getActivity()).thenReturn(activity);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

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

        when(browserSwitchIntent.getData()).thenReturn(uri);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(123, uri, fragmentListener, browserSwitchListener);

        verify(applicationContext).startActivity(browserSwitchIntent);

        ArgumentCaptor<BrowserSwitchRequest> captor =
            ArgumentCaptor.forClass(BrowserSwitchRequest.class);
        verify(persistentStore).putActiveRequest(captor.capture(), same(applicationContext));

        BrowserSwitchRequest browserSwitchRequest = captor.getValue();
        assertEquals(browserSwitchRequest.getRequestCode(), 123);
        assertEquals(browserSwitchRequest.getUri(), uri);
        assertEquals(browserSwitchRequest.getState(), BrowserSwitchRequest.PENDING);
    }

    @Test
    public void start_whenRequestCodeIsIntegerMinValue_notifiesListenerOfError() {
        when(fragmentListener.getActivity()).thenReturn(activity);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

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
        sut.start(Integer.MIN_VALUE, uri, fragmentListener, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
            ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(anyInt(), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_ERROR);
        assertEquals(result.getErrorMessage(), "Request code cannot be Integer.MIN_VALUE");
    }

    @Test
    public void start_whenIsNotConfiguredForBrowserSwitch_notifiesListenerOfError() {
        when(fragmentListener.getActivity()).thenReturn(activity);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

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
        sut.start(123, uri, fragmentListener, browserSwitchListener);

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
    public void start_whenNoActivityFoundCanOpenURL_notifiesListenerOfError() {
        when(fragmentListener.getActivity()).thenReturn(activity);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

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
        sut.start(123, uri, fragmentListener, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor = ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(eq(123), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_ERROR);
        assertEquals("No installed activities can open this URL: https://example.com/", result.getErrorMessage());
    }

    @Test
    public void startWithUri_whenFragmentIsNotBrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Fragment must implement BrowserSwitchListener.");

        Fragment fragment = mock(Fragment.class);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(123, uri, fragment);
    }

    @Test
    public void startWithUri_whenFragmentIsNotAttachedToAnActivity_throwsIllegalStateException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Fragment must be attached to an activity.");

        when(fragmentListener.getActivity()).thenReturn(null);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(123, uri, fragmentListener);
    }

    @Test
    public void startWithUri_whenActivityIsNotBrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Activity must implement BrowserSwitchListener.");

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(123, uri, activity);
    }

    @Test
    public void startWithIntent_whenFragmentIsNotABrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Fragment must implement BrowserSwitchListener.");

        Intent intent = mock(Intent.class);
        Fragment fragment = mock(Fragment.class);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(123, intent, fragment);
    }

    @Test
    public void startWithIntent_whenFragmentIsNotAttachedToAnActivity_throwsIllegalStateException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Fragment must be attached to an activity.");

        when(fragmentListener.getActivity()).thenReturn(null);

        Intent intent = mock(Intent.class);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(123, intent, fragmentListener);
    }

    @Test
    public void startWithIntent_whenActivityIsNotBrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Activity must implement BrowserSwitchListener.");

        Intent intent = mock(Intent.class);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.start(123, intent, activity);
    }

    @Test
    public void deliverResult_whenRequestIsSuccessful_clearsResultStoreAndNotifiesResultOK() {
        when(fragmentListener.getActivity()).thenReturn(activity);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitchRequest request =
            new BrowserSwitchRequest(123, uri, BrowserSwitchRequest.SUCCESS, null);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(fragmentListener, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(eq(123), captor.capture(), same(uri));

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_OK);
        assertNull(result.getErrorMessage());

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsPending_clearsResultStoreAndNotifiesResultCANCELLED() {
        when(fragmentListener.getActivity()).thenReturn(activity);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, uri, BrowserSwitchRequest.PENDING, null);
        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(request);

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(fragmentListener, browserSwitchListener);

        ArgumentCaptor<BrowserSwitchResult> captor =
                ArgumentCaptor.forClass(BrowserSwitchResult.class);
        verify(browserSwitchListener).onBrowserSwitchResult(eq(123), captor.capture(), isNull());

        BrowserSwitchResult result = captor.getValue();
        assertNotNull(result);
        assertEquals(result.getStatus(), BrowserSwitchResult.STATUS_CANCELED);
        assertNull(result.getErrorMessage());

        verify(persistentStore).clearActiveRequest(applicationContext);
    }

    @Test
    public void deliverResult_whenRequestIsNull_doesNothing() {
        when(fragmentListener.getActivity()).thenReturn(activity);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        when(persistentStore.getActiveRequest(applicationContext)).thenReturn(null);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(fragmentListener, browserSwitchListener);

        verify(browserSwitchListener, never()).onBrowserSwitchResult(anyInt(), any(), any());
        verify(persistentStore, never()).clearActiveRequest(activity);
    }

    @Test
    public void deliverResult_whenFragmentIsNotABrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Fragment must implement BrowserSwitchListener.");

        Fragment fragment = mock(Fragment.class);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(fragment);
    }

    @Test
    public void deliverResult_whenFragmentIsNotAttachedToAnActivity_throwsIllegalStateException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Fragment must be attached to an activity.");

        when(fragmentListener.getActivity()).thenReturn(null);
        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(fragmentListener);
    }

    @Test
    public void deliverResult_whenActivityIsNotABrowserSwitchListener_throwsIllegalArgumentException() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Activity must implement BrowserSwitchListener.");

        sut = BrowserSwitchClient.newInstance(browserSwitchConfig, activityFinder, persistentStore, returnUrlScheme);
        sut.deliverResult(activity);
    }

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
}
