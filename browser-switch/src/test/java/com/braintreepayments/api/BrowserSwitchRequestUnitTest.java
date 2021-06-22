package com.braintreepayments.api;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchRequestUnitTest {

    @Test
    public void fromJson_withoutShouldNotifyProperty_defaultsShouldNotifyToTrue() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\"\n" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);
        assertTrue(sut.getShouldNotifyCancellation());
    }

    @Test
    public void fromJson_withoutMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": true\n" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);

        assertEquals(sut.getRequestCode(), 123);
        assertEquals(sut.getUrl().toString(), "https://example.com");
        assertNull(sut.getMetadata());
        assertTrue(sut.getShouldNotifyCancellation());

        assertTrue(sut.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(sut.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }

    @Test
    public void toJson_withoutMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": false\n" +
                "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUrl(), original.getUrl());
        assertNull(restored.getMetadata());
        assertFalse(restored.getShouldNotifyCancellation());

        assertTrue(restored.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(restored.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }

    @Test
    public void fromJson_withMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": true,\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);

        assertEquals(sut.getRequestCode(), 123);
        assertEquals(sut.getUrl().toString(), "https://example.com");
        assertTrue(sut.getShouldNotifyCancellation());
        JSONAssert.assertEquals(sut.getMetadata(), new JSONObject().put("testKey", "testValue"), true);

        assertTrue(sut.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(sut.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }

    @Test
    public void toJson_withMetadata() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": false,\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUrl(), original.getUrl());
        assertFalse(restored.getShouldNotifyCancellation());
        JSONAssert.assertEquals(restored.getMetadata(), original.getMetadata(), true);

        assertTrue(restored.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(restored.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }
}
