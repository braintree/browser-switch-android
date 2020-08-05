package com.braintreepayments.browserswitch;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.braintreepayments.browserswitch.BrowserSwitchPersistentStore.REQUEST_KEY;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PersistentStore.class, BrowserSwitchRequest.class, Log.class })
public class BrowserSwitchPersistentStoreTest {

    private Context context;
    private BrowserSwitchRequest browserSwitchRequest;

    @Before
    public void beforeEach() {
        mockStatic(Log.class);
        mockStatic(PersistentStore.class);
        mockStatic(BrowserSwitchRequest.class);

        context = mock(Context.class);
        browserSwitchRequest = mock(BrowserSwitchRequest.class);
    }

    @Test
    public void getActiveRequest_whenPersistentStoreContainsRequest_parsesAndReturnsActiveRequest() throws JSONException {
        String activeRequestJson = "{\"active\":\"request\"}";
        when(PersistentStore.get(REQUEST_KEY, context)).thenReturn(activeRequestJson);
        when(BrowserSwitchRequest.fromJson(activeRequestJson)).thenReturn(browserSwitchRequest);

        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        BrowserSwitchRequest result = sut.getActiveRequest(context);
        assertSame(result, browserSwitchRequest);
    }

    @Test
    public void getActiveRequest_whenPersistentStoreDoesNotContainRequest_returnsNull() {
        when(PersistentStore.get(REQUEST_KEY, context)).thenReturn(null);
        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        BrowserSwitchRequest result = sut.getActiveRequest(context);
        assertNull(result);
    }

    @Test
    public void getActiveRequest_whenPersistentStoreContainsInvalidRequestJson_returnsNull() throws JSONException {
        when(PersistentStore.get(REQUEST_KEY, context)).thenReturn("invalid json");
        when(BrowserSwitchRequest.fromJson("invalid json")).thenThrow(new JSONException("json invalid"));

        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        BrowserSwitchRequest result = sut.getActiveRequest(context);
        assertNull(result);
    }

    @Test
    public void putActiveRequest_addsRequestJsonToPersistentStore() throws JSONException {
        String requestJson = "{\"request\":\"json\"}";
        when(browserSwitchRequest.toJson()).thenReturn(requestJson);

        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        sut.putActiveRequest(browserSwitchRequest, context);

        verifyStatic(PersistentStore.class);
        PersistentStore.put(REQUEST_KEY, requestJson, context);
    }

    @Test
    public void putActiveRequest_whenRequestJsonIsInvalid_doesNothing() throws JSONException {
        when(browserSwitchRequest.toJson()).thenThrow(new JSONException("json invalid"));

        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        sut.putActiveRequest(browserSwitchRequest, context);

        verifyStatic(PersistentStore.class, never());
        PersistentStore.put(anyString(), anyString(), any(Context.class));
    }

    @Test
    public void clearActiveRequest_removesRequestFromPersistentStore() {
        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        sut.clearActiveRequest(context);

        verifyStatic(PersistentStore.class);
        PersistentStore.remove(REQUEST_KEY, context);
    }
}
