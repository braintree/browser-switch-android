package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchClientUnitTest {

    private BrowserSwitchInspector browserSwitchInspector;

    private ChromeCustomTabsInternalClient customTabsInternalClient;

    private Uri browserSwitchDestinationUrl;
    private Context applicationContext;

    private ComponentActivity componentActivity;

    @Before
    public void beforeEach() {
        browserSwitchInspector = mock(BrowserSwitchInspector.class);
        customTabsInternalClient = mock(ChromeCustomTabsInternalClient.class);

        browserSwitchDestinationUrl = Uri.parse("https://example.com/browser_switch_destination");

        ActivityController<ComponentActivity> componentActivityController =
                Robolectric.buildActivity(ComponentActivity.class).setup();
        componentActivity = spy(componentActivityController.get());

        applicationContext = componentActivity.getApplicationContext();
    }

    @Test
    public void start_whenActivityIsFinishing_throwsException() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        when(componentActivity.isFinishing()).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);

        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals(((BrowserSwitchStartResult.Failure) request).getError().getMessage(), "Unable to start browser switch while host Activity is finishing.");
    }

    @Test
    public void start_whenSuccessful_returnsBrowserSwitchRequest() throws BrowserSwitchException, JSONException {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .launchType(LaunchType.ACTIVITY_CLEAR_TOP)
                .metadata(metadata);
        BrowserSwitchStartResult browserSwitchPendingRequest = sut.start(componentActivity, options);

        verify(customTabsInternalClient).launchUrl(componentActivity, browserSwitchDestinationUrl, LaunchType.ACTIVITY_CLEAR_TOP);

        assertNotNull(browserSwitchPendingRequest);
        assertTrue(browserSwitchPendingRequest instanceof BrowserSwitchStartResult.Started);

        String pendingRequest =
                ((BrowserSwitchStartResult.Started) browserSwitchPendingRequest).getPendingRequest();
        BrowserSwitchRequest browserSwitchRequest =
                BrowserSwitchRequest.fromBase64EncodedJSON(pendingRequest);

        assertEquals(browserSwitchRequest.getRequestCode(), 123);
        assertEquals(browserSwitchRequest.getUrl(), browserSwitchDestinationUrl);
        JSONAssert.assertEquals(browserSwitchRequest.getMetadata(), metadata, false);
    }

    @Test
    public void start_whenNoBrowserAvailable_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);
        doThrow(new ActivityNotFoundException()).when(customTabsInternalClient).launchUrl(any(Context.class), any(Uri.class), eq(null));

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);
        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals(((BrowserSwitchStartResult.Failure) request).getError().getMessage(), "Unable to start browser switch without a web browser.");
    }

    @Test
    public void start_whenRequestCodeIsIntegerMinValue_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(Integer.MIN_VALUE)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);
        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals(((BrowserSwitchStartResult.Failure) request).getError().getMessage(), "Request code cannot be Integer.MIN_VALUE");
    }

    @Test
    public void start_whenDeviceIsNotConfiguredForDeepLinking_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(false);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);

        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals("The return url scheme was not set up, incorrectly set up, or more than one " +
                "Activity on this device defines the same url scheme in it's Android Manifest. " +
                "See https://github.com/braintree/browser-switch-android for more information on " +
                "setting up a return url scheme.", ((BrowserSwitchStartResult.Failure) request).getError().getMessage());
    }

    @Test
    public void start_whenNoAppLinkUriOrReturnUrlSchemeSet_throwsError() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .returnUrlScheme(null)
                .appLinkUri(null)
                .url(browserSwitchDestinationUrl)
                .metadata(metadata);
        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals("An appLinkUri or returnUrlScheme is required.", ((BrowserSwitchStartResult.Failure) request).getError().getMessage());
    }

    @Test
    public void completeRequest_whenAppLinkMatches_successReturnedWithAppLink() throws BrowserSwitchException, JSONException {
        Uri appLinkUri = Uri.parse("https://example.com");
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(
                123,
                browserSwitchDestinationUrl,
                requestMetadata,
                null,
                appLinkUri
        );

        Intent intent = new Intent(Intent.ACTION_VIEW, appLinkUri);
        BrowserSwitchFinalResult result =
                sut.completeRequest(intent, request.toBase64EncodedJSON());

        assertTrue(result instanceof BrowserSwitchFinalResult.Success);

        BrowserSwitchFinalResult.Success successResult =
                (BrowserSwitchFinalResult.Success) result;
        assertEquals(appLinkUri, successResult.getReturnUrl());
        assertEquals(123, successResult.getRequestCode());
        JSONAssert.assertEquals(requestMetadata, successResult.getRequestMetadata(), true);
        assertEquals(browserSwitchDestinationUrl, successResult.getRequestUrl());
    }

    @Test
    public void completeRequest_whenActiveRequestMatchesDeepLinkResultURLScheme_returnsBrowserSwitchSuccessResult() throws BrowserSwitchException, JSONException {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(
                123,
                browserSwitchDestinationUrl,
                requestMetadata,
                "fake-url-scheme",
                null
        );

        Uri deepLinkUrl = Uri.parse("fake-url-scheme://success");
        Intent intent = new Intent(Intent.ACTION_VIEW, deepLinkUrl);
        BrowserSwitchFinalResult result =
                sut.completeRequest(intent, request.toBase64EncodedJSON());

        assertTrue(result instanceof BrowserSwitchFinalResult.Success);

        BrowserSwitchFinalResult.Success successResult =
                (BrowserSwitchFinalResult.Success) result;
        assertEquals(deepLinkUrl, successResult.getReturnUrl());
        assertEquals(123, successResult.getRequestCode());
        JSONAssert.assertEquals(requestMetadata, successResult.getRequestMetadata(), true);
        assertEquals(browserSwitchDestinationUrl, successResult.getRequestUrl());
    }

    @Test
    public void completeRequest_whenDeepLinkResultURLSchemeDoesntMatch_returnsNoResult() throws BrowserSwitchException {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request = new BrowserSwitchRequest(
                123,
                browserSwitchDestinationUrl,
                requestMetadata,
                "fake-url-scheme",
                null
        );

        Uri deepLinkUrl = Uri.parse("a-different-url-scheme://success");
        Intent intent = new Intent(Intent.ACTION_VIEW, deepLinkUrl);
        BrowserSwitchFinalResult result =
                sut.completeRequest(intent, request.toBase64EncodedJSON());

        assertTrue(result instanceof BrowserSwitchFinalResult.NoResult);
    }

    @Test
    public void completeRequest_whenIntentIsNull_returnsNoResult() throws BrowserSwitchException {
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                customTabsInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "fake-url-scheme", null);

        BrowserSwitchFinalResult result =
                sut.completeRequest(null, request.toBase64EncodedJSON());
        assertTrue(result instanceof BrowserSwitchFinalResult.NoResult);
    }
}
