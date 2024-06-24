package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

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
    }

    @Test
    public void fromJson_withoutMetadata_or_appLinkUri() throws JSONException {
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
        assertNull(sut.getAppLinkUri());

        assertTrue(sut.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(sut.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }

    @Test
    public void toJson_withoutMetadata_or_appLinkUri() throws JSONException {
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
        assertNull(restored.getAppLinkUri());

        assertTrue(restored.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(restored.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }

    @Test
    public void fromJson_withMetadata_and_appLinkUri() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": true,\n" +
                "  \"appLinkUri\": \"https://example.com\",\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest sut = BrowserSwitchRequest.fromJson(json);

        assertEquals(sut.getRequestCode(), 123);
        assertEquals(sut.getUrl().toString(), "https://example.com");
        assertEquals(sut.getAppLinkUri().toString(), "https://example.com");
        JSONAssert.assertEquals(sut.getMetadata(), new JSONObject().put("testKey", "testValue"), true);

        assertTrue(sut.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(sut.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }

    @Test
    public void toJson_withMetadata_and_appLinkUri() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": false,\n" +
                "  \"appLinkUri\": \"https://example.com\",\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUrl(), original.getUrl());
        assertEquals("https://example.com", restored.getAppLinkUri().toString());
        JSONAssert.assertEquals(restored.getMetadata(), original.getMetadata(), true);

        assertTrue(restored.matchesDeepLinkUrlScheme(Uri.parse("my-return-url-scheme://test")));
        assertFalse(restored.matchesDeepLinkUrlScheme(Uri.parse("another-return-url-scheme://test")));
    }

    @Test
    public void toJson_for_appLinkUri_without_returnUrlScheme() throws JSONException {
        String json = "{\n" +
            "  \"requestCode\": 123,\n" +
            "  \"url\": \"https://example.com\",\n" +
            "  \"shouldNotify\": false,\n" +
            "  \"appLinkUri\": \"https://example.com\",\n" +
            "  \"metadata\": {\n" +
            "    \"testKey\": \"testValue\"" +
            "  }" +
            "}";
        BrowserSwitchRequest original = BrowserSwitchRequest.fromJson(json);
        BrowserSwitchRequest restored = BrowserSwitchRequest.fromJson(original.toJson());

        assertEquals(restored.getRequestCode(), original.getRequestCode());
        assertEquals(restored.getUrl(), original.getUrl());
        assertEquals("https://example.com", restored.getAppLinkUri().toString());
        JSONAssert.assertEquals(restored.getMetadata(), original.getMetadata(), true);
    }

    @Test
    public void matchesDeepLinkUrlScheme_whenSameSchemeDifferentCase_returnsTrue() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": false,\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest request = BrowserSwitchRequest.fromJson(json);
        assertTrue(request.matchesDeepLinkUrlScheme(Uri.parse("My-Return-Url-Scheme://example.com")));
    }
    @Test
    public void matchesDeepLinkUrlScheme_whenDifferentScheme_returnsFalse() throws JSONException {
        String json = "{\n" +
                "  \"requestCode\": 123,\n" +
                "  \"url\": \"https://example.com\",\n" +
                "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
                "  \"shouldNotify\": false,\n" +
                "  \"metadata\": {\n" +
                "    \"testKey\": \"testValue\"" +
                "  }" +
                "}";
        BrowserSwitchRequest request = BrowserSwitchRequest.fromJson(json);
        assertFalse(request.matchesDeepLinkUrlScheme(Uri.parse("not-my-return-url-scheme://example.com")));
    }

    @Test
    public void matchesAppLinkUri_whenTheSame_returnsTrue() throws JSONException {
        String json = "{\n" +
            "  \"requestCode\": 123,\n" +
            "  \"url\": \"https://example.com\",\n" +
            "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
            "  \"appLinkUri\": \"https://example.com\",\n" +
            "  \"shouldNotify\": false,\n" +
            "  \"metadata\": {\n" +
            "    \"testKey\": \"testValue\"" +
            "  }" +
            "}";
        BrowserSwitchRequest request = BrowserSwitchRequest.fromJson(json);
        assertTrue(request.matchesAppLinkUri(Uri.parse("https://example.com")));
    }

    @Test
    public void matchesAppLinkUri_whenTheSame_withQueryParams_returnsTrue() throws JSONException {
        String json = "{\n" +
            "  \"requestCode\": 123,\n" +
            "  \"url\": \"https://example.com\",\n" +
            "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
            "  \"appLinkUri\": \"https://example.com\",\n" +
            "  \"shouldNotify\": false,\n" +
            "  \"metadata\": {\n" +
            "    \"testKey\": \"testValue\"" +
            "  }" +
            "}";
        BrowserSwitchRequest request = BrowserSwitchRequest.fromJson(json);
        assertTrue(request.matchesAppLinkUri(Uri.parse("https://example.com?isAppLink=true")));
    }

    @Test
    public void matchesAppLinkUri_whenSchemeIsDifferent_returnsFalse() throws JSONException {
        String json = "{\n" +
            "  \"requestCode\": 123,\n" +
            "  \"url\": \"https://example.com\",\n" +
            "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
            "  \"appLinkUri\": \"http://example.com\",\n" +
            "  \"shouldNotify\": false,\n" +
            "  \"metadata\": {\n" +
            "    \"testKey\": \"testValue\"" +
            "  }" +
            "}";
        BrowserSwitchRequest request = BrowserSwitchRequest.fromJson(json);
        assertFalse(request.matchesAppLinkUri(Uri.parse("https://example.com")));
    }

    @Test
    public void matchesAppLinkUri_whenHostIsDifferent_returnsFalse() throws JSONException {
        String json = "{\n" +
            "  \"requestCode\": 123,\n" +
            "  \"url\": \"https://example.com\",\n" +
            "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
            "  \"appLinkUri\": \"https://example.com\",\n" +
            "  \"shouldNotify\": false,\n" +
            "  \"metadata\": {\n" +
            "    \"testKey\": \"testValue\"" +
            "  }" +
            "}";
        BrowserSwitchRequest request = BrowserSwitchRequest.fromJson(json);
        assertFalse(request.matchesAppLinkUri(Uri.parse("https://another-example.com")));
    }

    @Test
    public void matchesAppLinkUri_whenNull_returnsFalse() throws JSONException {
        String json = "{\n" +
            "  \"requestCode\": 123,\n" +
            "  \"url\": \"https://example.com\",\n" +
            "  \"returnUrlScheme\": \"my-return-url-scheme\",\n" +
            "  \"shouldNotify\": false,\n" +
            "  \"metadata\": {\n" +
            "    \"testKey\": \"testValue\"" +
            "  }" +
            "}";
        BrowserSwitchRequest request = BrowserSwitchRequest.fromJson(json);
        assertNull(request.getAppLinkUri());
        assertFalse(request.matchesAppLinkUri(Uri.parse("https://example.com")));
    }
}
