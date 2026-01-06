package com.braintreepayments.api

import android.net.Uri
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrowserSwitchRequestUnitTest {

    @Test
    fun startedConstructor_fromString_createsBrowserSwitchRequest() {
        val browserSwitchRequest = BrowserSwitchRequest(
            1,
            Uri.parse("http://target-uri.com"),
            JSONObject().put("test_key", "test_value"),
            "return-url-scheme",
            Uri.parse("https://app-link-uri.com")
        )

        val token = browserSwitchRequest.toBase64EncodedJSON()
        val sut = BrowserSwitchRequest.fromBase64EncodedJSON(token)
        assertEquals(browserSwitchRequest.requestCode, sut.requestCode)
        assertEquals("test_value", sut.metadata.getString("test_key"))
        assertEquals(Uri.parse("http://target-uri.com"), sut.url)
        assertEquals("return-url-scheme", sut.returnUrlScheme)
        assertEquals(Uri.parse("https://app-link-uri.com"), sut.appLinkUri)
    }
}
