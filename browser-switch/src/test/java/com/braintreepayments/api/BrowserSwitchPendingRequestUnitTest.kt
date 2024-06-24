package com.braintreepayments.api

import android.net.Uri
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrowserSwitchPendingRequestUnitTest {

    private val browserSwitchRequest = BrowserSwitchRequest(
        1,
        Uri.parse("http://"),
        JSONObject().put("test_key", "test_value"),
        "return-url-scheme",
        Uri.parse("https://example.com")
    )

    @Test
    fun startedConstructor_fromString_createsBrowserSwitchRequest() {
        val pendingRequest = BrowserSwitchPendingRequest.Started(browserSwitchRequest)
        val storedRequest = pendingRequest.toJsonString()

        val sut = BrowserSwitchPendingRequest.Started(storedRequest)
        assertEquals(browserSwitchRequest.requestCode, sut.browserSwitchRequest.requestCode)
        assertEquals(
            browserSwitchRequest.metadata.getString("test_key"),
            sut.browserSwitchRequest.metadata.getString("test_key")
        )
        assertEquals(browserSwitchRequest.url, sut.browserSwitchRequest.url)
        assertEquals(browserSwitchRequest.returnUrlScheme, sut.browserSwitchRequest.returnUrlScheme)
    }

    @Test(expected = JSONException::class)
    fun startedConstructor_fromString_whenInvalidString_throwsJSONException() {
        val sut = BrowserSwitchPendingRequest.Started("{}")
    }

    @Test
    fun toJsonString_returnsJsonBrowserSwitchRequest() {
        val sut = BrowserSwitchPendingRequest.Started(browserSwitchRequest)
        val jsonString = sut.toJsonString()

        assertEquals(browserSwitchRequest.toJson(), jsonString)
    }
}