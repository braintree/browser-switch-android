package com.braintreepayments.api

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.EngagementSignalsCallback
import androidx.browser.customtabs.ExperimentalMinimizationCallback


internal class ChromeCustomTabsInternalClient(
    private val customTabsIntentBuilder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
) {
    private var customTabsServiceConnection: CustomTabsServiceConnection? = null
    private var targetActivity: ComponentActivity? = null

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

    @OptIn(ExperimentalMinimizationCallback::class)
    fun openCustomTab(
        activity: ComponentActivity,
        url: Uri,
        launchAsNewTask: Boolean,
        browserSwitchCallbacks: BrowserSwitchCallbacks?
    ) {
        targetActivity = activity
        val customTabsCallback = object : CustomTabsCallback() {
            override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                super.onNavigationEvent(navigationEvent, extras)

                when (navigationEvent) {
                    TAB_SHOWN -> Log.d("asdf", "tab shown")
                    TAB_HIDDEN -> Log.e("asdf", "TAB_HIDDEN")
                }
            }

            override fun onMinimized(extras: Bundle) {
                super.onMinimized(extras)
                browserSwitchCallbacks?.onMinimized?.invoke()
            }
        }

        val customTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d("asdf", "Service Disconnected")
            }

            @SuppressLint("RequiresFeature")
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                val customTabsSession = client.newSession(customTabsCallback)
                client.warmup(0L)

                try {
                    val engagementSignalsApiAvailable = customTabsSession?.isEngagementSignalsApiAvailable(Bundle.EMPTY)
                    if (engagementSignalsApiAvailable != true) {
                        Log.d(
                            "asdf", "CustomTab Engagement signals not available, make sure to use the " +
                                    "latest Chrome version and enable via chrome://flags/#cct-real-time-engagement-signals"
                        )
                        return
                    }
                    customTabsSession.setEngagementSignalsCallback(
                        getEngagementSignalsCallback(browserSwitchCallbacks),
                        Bundle.EMPTY
                    )
                } catch (e: RemoteException) {
                    Log.w("asdf", "The Service died while responding to the request.", e)
                } catch (e: UnsupportedOperationException) {
                    Log.w("asdf", "Engagement Signals API isn't supported by the browser.", e)
                }

                val customTabsIntent = CustomTabsIntent.Builder(customTabsSession).build()
                if (launchAsNewTask) {
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                customTabsIntent.launchUrl(activity, url)
            }
        }

        val packageName = "com.android.chrome"
        val isBound = CustomTabsClient.bindCustomTabsService(activity, packageName, customTabsServiceConnection)
        if (!isBound) {
            Log.d("asdf", "Failed to bind to Custom Tabs service")
        }

        activity.application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private fun getEngagementSignalsCallback(
        browserSwitchCallbacks: BrowserSwitchCallbacks?
    ): EngagementSignalsCallback =
        object : EngagementSignalsCallback {
            override fun onVerticalScrollEvent(isDirectionUp: Boolean, extras: Bundle) {
                Log.d("asdf", "onVerticalScrollEvent (isDirectionUp=$isDirectionUp)")
            }

            override fun onGreatestScrollPercentageIncreased(scrollPercentage: Int, extras: Bundle) {
                Log.d("asdf", "scroll percentage: $scrollPercentage%")
            }

            override fun onSessionEnded(didUserInteract: Boolean, extras: Bundle) {
                browserSwitchCallbacks?.onFinished?.invoke()
            }
        }


    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            if (activity == targetActivity) {
                cleanUp(activity)
            }
        }
    }

    private fun cleanUp(activity: Activity) {
        customTabsServiceConnection?.let {
            activity.unbindService(it)
            customTabsServiceConnection = null
        }
        targetActivity = null
        Log.d("asdf", "Service unbound and cleaned up")
    }
}
