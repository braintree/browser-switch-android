package com.braintreepayments.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChromeCustomTabsInternalClientUnitTest {

    private lateinit var builder: CustomTabsIntent.Builder
    private lateinit var customTabsIntent: CustomTabsIntent
    private lateinit var context: Context
    private lateinit var url: Uri

    @Before
    fun setUp() {
        clearAllMocks()
        builder = mockk(relaxed = true)
        context = mockk(relaxed = true)
        url = mockk(relaxed = true)
        customTabsIntent = CustomTabsIntent.Builder().build()
        every { builder.build() } returns customTabsIntent
    }

    @Test
    fun `launchUrl with null LaunchType does not add flags`() {
        val client = ChromeCustomTabsInternalClient(builder)
        val intent = customTabsIntent.intent

        client.launchUrl(context, url, null)

        assertEquals(0, intent.flags)
    }

    @Test
    fun `launchUrl with ACTIVITY_NEW_TASK adds new task flag`() {
        val client = ChromeCustomTabsInternalClient(builder)
        val intent = customTabsIntent.intent

        client.launchUrl(context, url, LaunchType.ACTIVITY_NEW_TASK)

        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `launchUrl with ACTIVITY_CLEAR_TOP adds clear top flag`() {
        val client = ChromeCustomTabsInternalClient(builder)
        val intent = customTabsIntent.intent

        client.launchUrl(context, url, LaunchType.ACTIVITY_CLEAR_TOP)

        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
    }
}
