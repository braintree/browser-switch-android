package com.braintreepayments.api

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

internal class ChromeCustomTabsInternalClient(
    private val customTabsIntentBuilder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
) {

    @Throws(ActivityNotFoundException::class)
    fun launchUrl(
        context: Context,
        url: Uri,
        launchAsNewTask: Boolean
    ) {
        val customTabsIntent = customTabsIntentBuilder.build()
        if (launchAsNewTask) {
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        customTabsIntent.launchUrl(context, url)
    }
}
