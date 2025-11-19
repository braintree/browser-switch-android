package com.braintreepayments.api

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.auth.AuthTabIntent
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent

internal class AuthTabInternalClient (
    private val authTabIntentBuilder: AuthTabIntent.Builder = AuthTabIntent.Builder(),
    private val customTabsIntentBuilder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
) {

    fun isAuthTabSupported(context: Context): Boolean {
        val packageName = CustomTabsClient.getPackageName(context, null)
        return when (packageName) {
            null -> false
            else -> CustomTabsClient.isAuthTabSupported(context, packageName)
        }
    }

    /**
     * Launch URL using Auth Tab if supported, otherwise fall back to Custom Tabs
     */
    @Throws(ActivityNotFoundException::class)
    fun launchUrl(
        context: Context,
        url: Uri,
        returnUrlScheme: String?,
        appLinkUri: Uri?,
        successAppLinkUri: Uri?,
        launcher: ActivityResultLauncher<Intent>?,
        launchType: LaunchType?
    ) {
        val useAuthTab = isAuthTabSupported(context)

        if (useAuthTab && launcher != null) {
            val authTabIntent = authTabIntentBuilder.build()

            if (launchType == LaunchType.ACTIVITY_CLEAR_TOP) {
                authTabIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            successAppLinkUri?.host?.let { host ->
                val path = successAppLinkUri.path ?: "/"
                authTabIntent.launch(launcher, url, host, path)
            } ?: returnUrlScheme?.let {
                authTabIntent.launch(launcher, url, returnUrlScheme)
            } ?: throw IllegalArgumentException("Either returnUrlScheme or appLinkUri must be provided")
        } else {
            // fall back to Custom Tabs
            launchCustomTabs(context, url, launchType)
        }
    }
    private fun launchCustomTabs(context: Context, url: Uri, launchType: LaunchType?) {
        val customTabsIntent = customTabsIntentBuilder.build()
        when (launchType) {
            LaunchType.ACTIVITY_NEW_TASK -> {
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            LaunchType.ACTIVITY_CLEAR_TOP -> {
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            null -> { }
        }
        customTabsIntent.launchUrl(context, url)
    }
}