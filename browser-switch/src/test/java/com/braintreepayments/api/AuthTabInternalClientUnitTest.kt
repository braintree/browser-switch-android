package com.braintreepayments.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.auth.AuthTabIntent
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthTabInternalClientUnitTest {

    private lateinit var authTabBuilder: AuthTabIntent.Builder
    private lateinit var customTabsBuilder: CustomTabsIntent.Builder
    private lateinit var authTabIntent: AuthTabIntent
    private lateinit var customTabsIntent: CustomTabsIntent
    private lateinit var context: Context
    private lateinit var url: Uri
    private lateinit var launcher: ActivityResultLauncher<Intent>

    @Before
    fun setUp() {
        clearAllMocks()
        authTabBuilder = mockk(relaxed = true)
        customTabsBuilder = mockk(relaxed = true)
        context = mockk(relaxed = true)
        url = mockk(relaxed = true)
        launcher = mockk(relaxed = true)

        authTabIntent = AuthTabIntent.Builder().build()
        customTabsIntent = CustomTabsIntent.Builder().build()

        every { authTabBuilder.build() } returns authTabIntent
        every { customTabsBuilder.build() } returns customTabsIntent

        mockkStatic(CustomTabsClient::class)
    }

    @Test
    fun `isAuthTabSupported returns false when no browser package available`() {
        every { CustomTabsClient.getPackageName(context, null) } returns null

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)

        assertFalse(client.isAuthTabSupported(context))
    }

    @Test
    fun `isAuthTabSupported returns true when browser supports Auth Tab`() {
        val packageName = "com.android.chrome"
        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns true

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)

        assertTrue(client.isAuthTabSupported(context))
    }

    @Test
    fun `isAuthTabSupported returns false when browser does not support Auth Tab`() {
        val packageName = "com.android.chrome"
        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns false

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)

        assertFalse(client.isAuthTabSupported(context))
    }

    @Test
    fun `launchUrl uses Auth Tab with app link when supported`() {
        val appLinkUri = Uri.parse("https://example.com/auth")
        val packageName = "com.android.chrome"
        customTabsIntent = mockk(relaxed = true)
        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns true

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)

        client.launchUrl(context, url, null, appLinkUri, launcher, null)

        verify {
            authTabIntent.launch(launcher, url, "example.com", "/auth")
        }
        verify(exactly = 0) {
            customTabsIntent.launchUrl(any(), any())
        }
    }

    @Test
    fun `launchUrl uses Auth Tab with return URL scheme when supported`() {
        val returnUrlScheme = "testcustomscheme"
        val packageName = "com.android.chrome"
        customTabsIntent = mockk(relaxed = true)
        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns true

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)

        client.launchUrl(context, url, returnUrlScheme, null, launcher, null)

        verify {
            authTabIntent.launch(launcher, url, returnUrlScheme)
        }
        verify(exactly = 0) {
            customTabsIntent.launchUrl(any(), any())
        }
    }

    @Test
    fun `launchUrl adds CLEAR_TOP flag to Auth Tab when LaunchType is ACTIVITY_CLEAR_TOP`() {
        val returnUrlScheme = "example"
        val packageName = "com.android.chrome"

        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns true

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)
        val intent = authTabIntent.intent

        client.launchUrl(context, url, returnUrlScheme, null, launcher, LaunchType.ACTIVITY_CLEAR_TOP)

        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)

        verify {
            authTabIntent.launch(launcher, url, returnUrlScheme)
        }
    }

    @Test
    fun `launchUrl falls back to Custom Tabs when Auth Tab not supported`() {
        authTabIntent = mockk(relaxed = true)
        every { authTabBuilder.build() } returns authTabIntent

        val returnUrlScheme = "example"
        val packageName = "com.android.chrome"

        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns false

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)

        client.launchUrl(context, url, returnUrlScheme, null, launcher, null)

        verify {
            customTabsIntent.launchUrl(context, url)
        }
        verify(exactly = 0) {
            authTabIntent.launch(any(), any(), any<String>())
        }
    }

    @Test
    fun `launchUrl handles app link with no path`() {
        val appLinkUri = Uri.parse("https://example.com")
        val packageName = "com.android.chrome"

        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns true

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)

        client.launchUrl(context, url, null, appLinkUri, launcher, null)

        verify {
            authTabIntent.launch(launcher, url, "example.com", "/")
        }
    }

    @Test
    fun `launchUrl with null LaunchType does not add flags to Custom Tabs`() {
        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)
        val intent = customTabsIntent.intent
        val returnUrlScheme = "example"

        // Force AuthTab not to be supported to fall back to Custom Tabs
        every { CustomTabsClient.getPackageName(context, null) } returns null

        client.launchUrl(context, url, returnUrlScheme, null, launcher, null)

        assertEquals(0, intent.flags)
    }

    @Test
    fun `launchUrl with ACTIVITY_NEW_TASK adds new task flag to Custom Tabs`() {
        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)
        val intent = customTabsIntent.intent
        val returnUrlScheme = "example"

        // Force AuthTab not to be supported to fall back to Custom Tabs
        every { CustomTabsClient.getPackageName(context, null) } returns null

        client.launchUrl(context, url, returnUrlScheme, null, launcher, LaunchType.ACTIVITY_NEW_TASK)

        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `launchUrl with ACTIVITY_CLEAR_TOP adds clear top flag to Custom Tabs`() {
        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)
        val intent = customTabsIntent.intent
        val returnUrlScheme = "example"

        // Force AuthTab not to be supported to fall back to Custom Tabs
        every { CustomTabsClient.getPackageName(context, null) } returns null

        client.launchUrl(context, url, returnUrlScheme, null, launcher, LaunchType.ACTIVITY_CLEAR_TOP)

        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
    }

    @Test
    fun `launchUrl with null launcher falls back to Custom Tabs even when Auth Tab is supported`() {
        val packageName = "com.android.chrome"
        every { CustomTabsClient.getPackageName(context, null) } returns packageName
        every { CustomTabsClient.isAuthTabSupported(context, packageName) } returns true

        val client = AuthTabInternalClient(authTabBuilder, customTabsBuilder)
        val returnUrlScheme = "example"

        client.launchUrl(context, url, returnUrlScheme, null, null, null)

        verify {
            customTabsIntent.launchUrl(context, url)
        }
    }
}
