package com.braintreepayments.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchRequestUnitTest {

    @Test
    public void fromJson_withoutMetadata() throws JSONException {

        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\"\n" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);

        assertEquals(sut.getRequestCode(), 123);
        assertEquals(sut.getUrl().toString(), "https://example.com");
        assertNull(sut.getMetadata());
    }

    @Test
    public void toJson_withoutMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\"\n" +
                "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUrl(), original.getUrl());
        assertNull(restored.getMetadata());
    }

    @Test
    public void fromJson_withMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);

        assertEquals(sut.getRequestCode(), 123);
        assertEquals(sut.getUrl().toString(), "https://example.com");
        JSONAssert.assertEquals(sut.getMetadata(), new JSONObject().put("testKey", "testValue"), true);
    }

    @Test
    public void toJson_withMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUrl(), original.getUrl());
        JSONAssert.assertEquals(restored.getMetadata(), original.getMetadata(), true);
    }
}
