package com.braintreepayments.browserswitch;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchRequestTest {

    @Test
    public void fromJson_withoutMetadata() throws JSONException {

        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"state\": \"PENDING\"\n" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);

        assertEquals(sut.getRequestCode(), 123);
        assertEquals(sut.getUri().toString(), "https://example.com");
        assertEquals(sut.getState(), BrowserSwitchRequest.PENDING);
        assertNull(sut.getMetadata());
    }

    @Test
    public void toJson_withoutMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"state\": \"SUCCESS\"\n" +
                "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUri(), original.getUri());
        assertEquals(restored.getState(), original.getState());
        assertNull(restored.getMetadata());
    }

    @Test
    public void fromJson_withMetadata() throws JSONException {

        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"state\": \"PENDING\",\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);

        assertEquals(sut.getRequestCode(), 123);
        assertEquals(sut.getUri().toString(), "https://example.com");
        assertEquals(sut.getState(), BrowserSwitchRequest.PENDING);
        JSONAssert.assertEquals(sut.getMetadata(), new JSONObject().put("testKey", "testValue"), true);
    }

    @Test
    public void toJson_withMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"state\": \"SUCCESS\",\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUri(), original.getUri());
        assertEquals(restored.getState(), original.getState());
        JSONAssert.assertEquals(restored.getMetadata(), original.getMetadata(), true);
    }
}

