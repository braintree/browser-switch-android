package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchResultUnitTest {

    @Test
    public void toJSON_serializesResult() throws JSONException {
        Uri requestUrl = Uri.parse("https://www.example.com");
        Uri appLinkUri = Uri.parse("https://www.example.com");
        String returnUrlScheme = "example.return.url.scheme";
        JSONObject requestMetadata = new JSONObject()
            .put("sample", "value");
        BrowserSwitchRequest request =
            new BrowserSwitchRequest(123, requestUrl, requestMetadata, returnUrlScheme, appLinkUri, true);

        Uri deepLinkUrl = Uri.parse("example.return.url.scheme://success/ok");
        BrowserSwitchResult sut = new BrowserSwitchResult(BrowserSwitchStatus.SUCCESS, request, deepLinkUrl);

        BrowserSwitchResult sutSerialized = BrowserSwitchResult.fromJson(sut.toJson());

        assertEquals(BrowserSwitchStatus.SUCCESS, sutSerialized.getStatus());
        assertEquals(deepLinkUrl, sutSerialized.getDeepLinkUrl());

        assertEquals(123, sutSerialized.getRequestCode());
        assertEquals(requestUrl, sutSerialized.getRequestUrl());
        JSONAssert.assertEquals(requestMetadata, sutSerialized.getRequestMetadata(), true);
    }
}
