# Android Browser Switch v3 (Beta) Migration Guide

See the [CHANGELOG](/CHANGELOG.md) for a complete list of changes. This migration guide outlines the basics for updating your browser switch integration from v2 to v3.

_Documentation for v2 will be published to https://developers.braintreepayments.com once it is available for general release._

## What's New

This version of `browser-switch` simplifies the API surface and allows for more flexibility with integration patterns by giving the integrating app control over storage and retrieval of the pending browser switch request. 

The minimum Android API for v3 is 23. This version of the SDK uses Java 11 and Kotlin 1.9.10.

## Setup

The setup of this library has not changed from v2 to v3.

Add the library to your dependencies in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:browser-switch:3.0.0-beta1'
}
```

Then, add an `intent-filter` in the `AndroidManifest.xml` to your deep link destination activity:

```xml
<activity android:name="com.company.app.MyBrowserSwitchActivity"
    android:exported="true">
    ...
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <data android:scheme="my-custom-url-scheme"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
    </intent-filter>
</activity>
```

## Usage

```kotlin
class MyActivity : ComponentActivity() {

    val browserSwitchClient: BrowserSwitchClient = BrowserSwitchClient()
    val pendingRequestState: String? = null

    override fun onResume() {
        super.onResume()
        handleReturnToAppFromBrowser(intent)
    }
    
    fun startBrowserSwitch() {
        val browserSwitchOptions = BrowserSwitchOptions().apply {
            requestCode = MY_REQUEST_CODE
            url = "https://example.com"
            returnUrlScheme = "my-custom-url-scheme"
        }
        when (val result = browserSwitchClient.start(this, browserSwitchOptions)) {
            is BrowserSwitchPendingRequest.Started -> { 
                // store pending request
                pendingRequestState = result.pendingRequestState
            }
            is BrowserSwitchPendingRequest.Failure -> { 
                // browser was unable to be launched, handle failure
                Log.d("MyActivity", result.error)
            }
        }
    }
    
    fun handleReturnToAppFromBrowser(intent: Intent) {
        if (pendingRequestState != null) {
            when (val result = browserSwitchClient.parseResult(intent, pendingRequestState)) {
                is BrowserSwitchParseResult.Success -> {
                    // handle successful browser switch result
                    // drop reference to pending request
                    pendingRequestState = null
                }
                is BrowserSwitchParseResult.Failure -> {
                    // browser switch parsing failed
                    Log.d("MyActivity", result.error)
                    // drop reference to pending request
                    pendingRequestState = null
                }
                is BrowserSwitchParseResult.NoResult -> {
                    // user did not complete browser switch
                    // allow user to complete browser switch, or clear stored pending request
                }
            }
        }
    }
}
```

## Launch Modes

If your deep link destination activity is configured in the `AndroidManifest.xml` with `android:launchMode="singleTop"`, `android:launchMode="singleTask"` or `android:launchMode="singleInstance"` handle the return to app from browser with the intent from `onNewIntent` rather than `onResume`.

```kotlin
class MySingleTopActivity : ComponentActivity() {

    val pendingRequestState: String? = null
    val browserSwitchClient: BrowserSwitchClient = BrowserSwitchClient()
    
    override fun onCreate() {
        super.onCreate()
        /**
         * TODO: initialize pendingRequestState from your app's preferred persistence store
         * e.g. shared prefs, data store or saved instance state
         */
        pendingRequestState = ...
    }

    override fun onResume() {
        super.onResume()
        
        // handle browser switch when deep link triggers a cold start of the app
        handleReturnToAppFromBrowser(intent, pendingRequestState)
    }
    
    override fun onNewIntent(newIntent: Intent) {
        // handle browser switch when deep link brings already running singleTop activity to the foreground
        handleReturnToAppFromBrowser(newIntent, pendingRequestState)
    }    
}
```