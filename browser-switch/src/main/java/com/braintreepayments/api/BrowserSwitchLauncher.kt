package com.braintreepayments.api

import android.content.ActivityNotFoundException
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner


class BrowserSwitchLauncher {

    private val BROWSER_SWITCH_RESULT = "com.braintreepayments.api.BrowserSwitch.RESULT"

    private var activityLauncher: ActivityResultLauncher<String>? = null

    constructor(
        activity: ComponentActivity,
        callback: BrowserSwitchLauncherCallback
    ) :
        this(activity.activityResultRegistry, activity, callback)


    constructor(
        fragment: Fragment,
        callback: BrowserSwitchLauncherCallback
    ) :
        this(
            fragment.requireActivity().activityResultRegistry, fragment.viewLifecycleOwner,
            callback
        )


    constructor(
        registry: ActivityResultRegistry, lifecycleOwner: LifecycleOwner,
        callback: BrowserSwitchLauncherCallback
    ) {
        activityLauncher = registry.register(
            BROWSER_SWITCH_RESULT,
            lifecycleOwner,
            BrowserSwitchActivityResultContract(), callback::onResult
        )
    }

    @Throws(BrowserSwitchException::class)
    fun launch() {
        try {
            activityLauncher?.launch("test")
        } catch (e: ActivityNotFoundException) {
            throw BrowserSwitchException(e.message)
        }
    }
}