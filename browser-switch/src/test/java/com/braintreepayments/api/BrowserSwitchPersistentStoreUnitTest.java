package com.braintreepayments.api;

import static com.braintreepayments.api.BrowserSwitchPersistentStore.REQUEST_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchPersistentStoreUnitTest {

    private Context context;
    private BrowserSwitchRequest browserSwitchRequest;

    @Before
    public void beforeEach() {
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        browserSwitchRequest = mock(BrowserSwitchRequest.class);
    }

    @Test
    public void getActiveRequest_whenPersistentStoreContainsRequest_parsesAndReturnsActiveRequest() throws JSONException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("requestCode", "1");
        jsonRequest.put("url", "http://");
        jsonRequest.put("returnUrlScheme", "my-scheme");
        jsonRequest.put("shouldNotify", "false");
        String activeRequestJson = jsonRequest.toString();

        BrowserSwitchRequest browserSwitchRequest = BrowserSwitchRequest.fromJson(activeRequestJson);
        PersistentStore.put(REQUEST_KEY, activeRequestJson, context);

        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        BrowserSwitchRequest result = sut.getActiveRequest(context);
        assertEquals(result.getMetadata(), browserSwitchRequest.getMetadata());
        assertEquals(result.getRequestCode(), browserSwitchRequest.getRequestCode());
        assertEquals(result.getUrl(), browserSwitchRequest.getUrl());
        assertEquals(result.getShouldNotifyCancellation(), browserSwitchRequest.getShouldNotifyCancellation());
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
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("not-browser-switch-field", "1");
        String activeRequestJson = jsonRequest.toString();

        PersistentStore.put(REQUEST_KEY, activeRequestJson, context);

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

        assertEquals(requestJson, PersistentStore.get(REQUEST_KEY, context));
    }

    @Test
    public void putActiveRequest_whenRequestJsonIsInvalid_doesNothing() throws JSONException {
        when(browserSwitchRequest.toJson()).thenThrow(new JSONException("json invalid"));

        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        sut.putActiveRequest(browserSwitchRequest, context);

        assertNull(PersistentStore.get(REQUEST_KEY, context));
    }

    @Test
    public void clearActiveRequest_removesRequestFromPersistentStore() {
        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        sut.clearActiveRequest(context);

        assertNull(PersistentStore.get(REQUEST_KEY, context));
    }

    @Test
    public void removeAll_removesBothRequestAndResponseItemsFromPersistentStore() throws JSONException {
        String requestJson = "{\"request\":\"json\"}";
        when(browserSwitchRequest.toJson()).thenReturn(requestJson);

        Uri deepLinkUrl = mock(Uri.class);
        BrowserSwitchResult result = new BrowserSwitchResult(123, browserSwitchRequest, deepLinkUrl);

        BrowserSwitchPersistentStore sut = BrowserSwitchPersistentStore.getInstance();
        sut.putActiveRequest(browserSwitchRequest, context);
        sut.putActiveResult(result, context);
        sut.removeAll(context);

        assertNull(sut.getActiveRequest(context));
        assertNull(sut.getActiveResult(context));
    }
}
