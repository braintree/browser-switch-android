package com.braintreepayments.api

import android.net.Uri
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
        false
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
        assertEquals(
            browserSwitchRequest.shouldNotifyCancellation,
            sut.browserSwitchRequest.shouldNotifyCancellation
        )
        assertEquals(browserSwitchRequest.returnUrlScheme, sut.browserSwitchRequest.returnUrlScheme)
    }

    @Test
    fun toJsonString_returnsJsonBrowserSwitchRequest() {
        val sut = BrowserSwitchPendingRequest.Started(browserSwitchRequest)
        val jsonString = sut.toJsonString()

        assertEquals(browserSwitchRequest.toJson(), jsonString)
    }
}