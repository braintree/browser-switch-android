package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.browser.auth.AuthTabIntent;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchClientUnitTest {

    private BrowserSwitchInspector browserSwitchInspector;
    private AuthTabInternalClient authTabInternalClient;
    private ActivityResultLauncher mockLauncher;

    private Uri browserSwitchDestinationUrl;
    private Context applicationContext;

    private ComponentActivity componentActivity;

    @Before
    public void beforeEach() {
        browserSwitchInspector = mock(BrowserSwitchInspector.class);
        authTabInternalClient = mock(AuthTabInternalClient.class);
        mockLauncher = mock(ActivityResultLauncher.class);

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
                authTabInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);

        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals("Unable to start browser switch while host Activity is finishing.",
                ((BrowserSwitchStartResult.Failure) request).getError().getMessage());
    }

    @Test
    public void start_whenSuccessful_returnsBrowserSwitchRequest() throws BrowserSwitchException, JSONException {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                authTabInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .launchType(LaunchType.ACTIVITY_CLEAR_TOP)
                .metadata(metadata);
        BrowserSwitchStartResult browserSwitchPendingRequest = sut.start(componentActivity, options);

        verify(authTabInternalClient).launchUrl(
                eq(componentActivity),
                eq(browserSwitchDestinationUrl),
                eq("return-url-scheme"),
                isNull(),
                isNull(),
                eq(LaunchType.ACTIVITY_CLEAR_TOP)
        );

        assertNotNull(browserSwitchPendingRequest);
        assertTrue(browserSwitchPendingRequest instanceof BrowserSwitchStartResult.Started);

        String pendingRequest =
                ((BrowserSwitchStartResult.Started) browserSwitchPendingRequest).getPendingRequest();
        BrowserSwitchRequest browserSwitchRequest =
                BrowserSwitchRequest.fromBase64EncodedJSON(pendingRequest);

        assertEquals(123, browserSwitchRequest.getRequestCode());
        assertEquals(browserSwitchDestinationUrl, browserSwitchRequest.getUrl());
        JSONAssert.assertEquals(browserSwitchRequest.getMetadata(), metadata, false);
    }

    @Test
    public void start_withAppLinkUri_passesItToAuthTab() {
        Uri appLinkUri = Uri.parse("https://example.com/auth");

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                authTabInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .appLinkUri(appLinkUri)
                .metadata(metadata);

        BrowserSwitchStartResult browserSwitchPendingRequest = sut.start(componentActivity, options);

        verify(authTabInternalClient).launchUrl(
                eq(componentActivity),
                eq(browserSwitchDestinationUrl),
                isNull(),
                eq(appLinkUri),
                isNull(),
                isNull()
        );

        assertTrue(browserSwitchPendingRequest instanceof BrowserSwitchStartResult.Started);
    }

    @Test
    public void start_whenNoBrowserAvailable_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);
        when(authTabInternalClient.isAuthTabSupported(any(Context.class))).thenReturn(false);
        doThrow(new ActivityNotFoundException()).when(authTabInternalClient).launchUrl(
                eq(componentActivity),
                eq(browserSwitchDestinationUrl),
                eq("return-url-scheme"),
                isNull(),
                isNull(),
                isNull()
        );

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                authTabInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);

        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals("Unable to start browser switch without a web browser.",
                ((BrowserSwitchStartResult.Failure) request).getError().getMessage());
    }

    @Test
    public void start_whenRequestCodeIsIntegerMinValue_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                authTabInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(Integer.MIN_VALUE)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);
        BrowserSwitchStartResult request = sut.start(componentActivity, options);
        assertTrue(request instanceof BrowserSwitchStartResult.Failure);
        assertEquals("Request code cannot be Integer.MIN_VALUE",
                ((BrowserSwitchStartResult.Failure) request).getError().getMessage());
    }

    @Test
    public void start_whenDeviceIsNotConfiguredForDeepLinking_returnsFailure() {
        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(applicationContext, "return-url-scheme")).thenReturn(false);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                authTabInternalClient);

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
                authTabInternalClient);

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
    public void initializeAuthTabLauncher_registersLauncherWithActivity() {

        try (MockedStatic<AuthTabIntent> mockedAuthTab = mockStatic(AuthTabIntent.class)) {

            mockedAuthTab.when(() -> AuthTabIntent.registerActivityResultLauncher(
                    any(ComponentActivity.class),
                    any(ActivityResultCallback.class)
            )).thenReturn(mockLauncher);

            BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, authTabInternalClient);
            BrowserSwitchClient.AuthTabCallback callback = mock(BrowserSwitchClient.AuthTabCallback.class);

            sut.initializeAuthTabLauncher(componentActivity, callback);

            mockedAuthTab.verify(() -> AuthTabIntent.registerActivityResultLauncher(
                    eq(componentActivity),
                    any(ActivityResultCallback.class)
            ));

            ArgumentCaptor<ActivityResultCallback<AuthTabIntent.AuthResult>> callbackCaptor =
                    ArgumentCaptor.forClass(ActivityResultCallback.class);
            mockedAuthTab.verify(() -> AuthTabIntent.registerActivityResultLauncher(
                    eq(componentActivity),
                    callbackCaptor.capture()
            ));

            assertNotNull(callbackCaptor.getValue());
        }
    }

    @Test
    public void start_withAuthTabLauncherInitialized_usesPendingAuthTabRequest() throws BrowserSwitchException {
        try (MockedStatic<AuthTabIntent> mockedAuthTab = mockStatic(AuthTabIntent.class)) {
            when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(
                    componentActivity.getApplicationContext(),
                    "return-url-scheme"
            )).thenReturn(true);
            when(authTabInternalClient.isAuthTabSupported(componentActivity)).thenReturn(true);

            ArgumentCaptor<ActivityResultCallback> callbackCaptor =
                    ArgumentCaptor.forClass(ActivityResultCallback.class);
            mockedAuthTab.when(() -> AuthTabIntent.registerActivityResultLauncher(
                    eq(componentActivity),
                    callbackCaptor.capture()
            )).thenReturn(mockLauncher);

            BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, authTabInternalClient);

            BrowserSwitchClient.AuthTabCallback callback = mock(BrowserSwitchClient.AuthTabCallback.class);
            sut.initializeAuthTabLauncher(componentActivity, callback);

            JSONObject metadata = new JSONObject();
            BrowserSwitchOptions options = new BrowserSwitchOptions()
                    .requestCode(123)
                    .url(browserSwitchDestinationUrl)
                    .returnUrlScheme("return-url-scheme")
                    .metadata(metadata);

            BrowserSwitchStartResult result = sut.start(componentActivity, options);

            assertTrue(result instanceof BrowserSwitchStartResult.Started);

            verify(authTabInternalClient).launchUrl(
                    eq(componentActivity),
                    eq(browserSwitchDestinationUrl),
                    eq("return-url-scheme"),
                    isNull(),
                    eq(mockLauncher),
                    isNull()
            );

            String pendingRequestString = ((BrowserSwitchStartResult.Started) result).getPendingRequest();
            assertNotNull(pendingRequestString);

            BrowserSwitchRequest decodedRequest = BrowserSwitchRequest.fromBase64EncodedJSON(pendingRequestString);
            assertEquals(123, decodedRequest.getRequestCode());
            assertEquals(browserSwitchDestinationUrl, decodedRequest.getUrl());
        }
    }

    @Test
    public void authTabCallback_withResultOK_callsCallbackWithSuccess() {
        try (MockedStatic<AuthTabIntent> mockedAuthTab = mockStatic(AuthTabIntent.class)) {

            ArgumentCaptor<ActivityResultCallback<AuthTabIntent.AuthResult>> callbackCaptor =
                    ArgumentCaptor.forClass(ActivityResultCallback.class);
            mockedAuthTab.when(() -> AuthTabIntent.registerActivityResultLauncher(
                    eq(componentActivity),
                    callbackCaptor.capture()
            )).thenReturn(mockLauncher);


            when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(
                    componentActivity.getApplicationContext(),
                    "return-url-scheme"
            )).thenReturn(true);
            when(authTabInternalClient.isAuthTabSupported(componentActivity)).thenReturn(true);

            BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, authTabInternalClient);
            BrowserSwitchClient.AuthTabCallback mockCallback = mock(BrowserSwitchClient.AuthTabCallback.class);

            sut.initializeAuthTabLauncher(componentActivity, mockCallback);

            JSONObject metadata = new JSONObject();
            BrowserSwitchOptions options = new BrowserSwitchOptions()
                    .requestCode(123)
                    .url(browserSwitchDestinationUrl)
                    .returnUrlScheme("return-url-scheme")
                    .metadata(metadata);
            sut.start(componentActivity, options);

            Uri resultUri = Uri.parse("return-url-scheme://success");
            AuthTabIntent.AuthResult mockAuthResult = mock(AuthTabIntent.AuthResult.class, withSettings()
                    .useConstructor(AuthTabIntent.RESULT_OK, resultUri)
                    .defaultAnswer(CALLS_REAL_METHODS));

            callbackCaptor.getValue().onActivityResult(mockAuthResult);

            ArgumentCaptor<BrowserSwitchFinalResult> resultCaptor =
                    ArgumentCaptor.forClass(BrowserSwitchFinalResult.class);
            verify(mockCallback).onResult(resultCaptor.capture());

            BrowserSwitchFinalResult capturedResult = resultCaptor.getValue();
            assertTrue(capturedResult instanceof BrowserSwitchFinalResult.Success);

            BrowserSwitchFinalResult.Success successResult =
                    (BrowserSwitchFinalResult.Success) capturedResult;
            assertEquals(resultUri, successResult.getReturnUrl());
            assertEquals(123, successResult.getRequestCode());
        }
    }

    @Test
    public void authTabCallback_withResultCanceled_callsCallbackWithNoResult() {
        try (MockedStatic<AuthTabIntent> mockedAuthTab = mockStatic(AuthTabIntent.class)) {
            ArgumentCaptor<ActivityResultCallback<AuthTabIntent.AuthResult>> callbackCaptor =
                    ArgumentCaptor.forClass(ActivityResultCallback.class);
            mockedAuthTab.when(() -> AuthTabIntent.registerActivityResultLauncher(
                    eq(componentActivity),
                    callbackCaptor.capture()
            )).thenReturn(mockLauncher);

            BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, authTabInternalClient);
            BrowserSwitchClient.AuthTabCallback mockCallback = mock(BrowserSwitchClient.AuthTabCallback.class);

            sut.initializeAuthTabLauncher(componentActivity, mockCallback);

            AuthTabIntent.AuthResult mockAuthResult = mock(AuthTabIntent.AuthResult.class, withSettings()
                    .useConstructor(AuthTabIntent.RESULT_CANCELED, null)
                    .defaultAnswer(CALLS_REAL_METHODS));

            callbackCaptor.getValue().onActivityResult(mockAuthResult);

            ArgumentCaptor<BrowserSwitchFinalResult> resultCaptor =
                    ArgumentCaptor.forClass(BrowserSwitchFinalResult.class);
            verify(mockCallback).onResult(resultCaptor.capture());

            BrowserSwitchFinalResult capturedResult = resultCaptor.getValue();
            assertTrue(capturedResult instanceof BrowserSwitchFinalResult.NoResult);
        }
    }

    @Test
    public void start_withoutAuthTabLauncher_fallsBackToCustomTabs() {

        when(browserSwitchInspector.isDeviceConfiguredForDeepLinking(
                componentActivity.getApplicationContext(),
                "return-url-scheme"
        )).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector, authTabInternalClient);

        JSONObject metadata = new JSONObject();
        BrowserSwitchOptions options = new BrowserSwitchOptions()
                .requestCode(123)
                .url(browserSwitchDestinationUrl)
                .returnUrlScheme("return-url-scheme")
                .metadata(metadata);

        BrowserSwitchStartResult result = sut.start(componentActivity, options);

        assertTrue(result instanceof BrowserSwitchStartResult.Started);

        verify(authTabInternalClient).launchUrl(
                eq(componentActivity),
                eq(browserSwitchDestinationUrl),
                eq("return-url-scheme"),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    public void isAuthTabSupported_delegatesToInternalClient() {
        when(authTabInternalClient.isAuthTabSupported(applicationContext)).thenReturn(true);

        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                authTabInternalClient);

        boolean result = sut.isAuthTabSupported(applicationContext);

        assertTrue(result);
        verify(authTabInternalClient).isAuthTabSupported(applicationContext);
    }


    @Test
    public void completeRequest_whenAppLinkMatches_successReturnedWithAppLink() throws BrowserSwitchException, JSONException {
        Uri appLinkUri = Uri.parse("https://example.com");
        BrowserSwitchClient sut = new BrowserSwitchClient(browserSwitchInspector,
                authTabInternalClient);

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
                authTabInternalClient);

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
                authTabInternalClient);

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
                authTabInternalClient);

        JSONObject requestMetadata = new JSONObject();
        BrowserSwitchRequest request =
                new BrowserSwitchRequest(123, browserSwitchDestinationUrl, requestMetadata, "fake-url-scheme", null);

        Intent mockIntent = mock(Intent.class);
        when(mockIntent.getData()).thenReturn(null);

        BrowserSwitchFinalResult result =
                sut.completeRequest(mockIntent, request.toBase64EncodedJSON());
        assertTrue(result instanceof BrowserSwitchFinalResult.NoResult);
    }
}