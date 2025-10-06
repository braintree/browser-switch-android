package com.braintreepayments.api

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import androidx.browser.auth.AuthTabIntent
import androidx.browser.customtabs.CustomTabsClient

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
class AuthTabInternalClient @VisibleForTesting constructor(
    private val authTabIntentBuilder: AuthTabIntent.Builder,
) {

    constructor() : this(AuthTabIntent.Builder())

    /**
     * Checks if Auth Tab is supported by the current browser
     */
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
        url: Uri,
        returnUrlScheme: String?,
        appLinkUri: Uri?,
        launcher: ActivityResultLauncher<Intent>?,
        launchType: LaunchType?
    ) {
            val authTabIntent = authTabIntentBuilder.build()

            if (launchType == LaunchType.ACTIVITY_CLEAR_TOP) {
                authTabIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            when {
                appLinkUri?.host != null -> {
                    val host = appLinkUri.host!!
                    val path = appLinkUri.path ?: "/"
                    authTabIntent.launch(launcher!!, url, host, path)
                }
                returnUrlScheme != null -> {
                    authTabIntent.launch(launcher!!, url, returnUrlScheme)
                }
                else -> {
                    throw IllegalArgumentException("Either returnUrlScheme or appLinkUri must be provided")
                }
            }
    }
}