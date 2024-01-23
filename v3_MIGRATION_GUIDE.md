# Android Browser Switch v3 (Beta) Migration Guide

See the [CHANGELOG](/CHANGELOG.md) for a complete list of changes. This migration guide outlines the basics for updating your browser switch integration from v2 to v3.

_Documentation for v2 will be published to https://developers.braintreepayments.com once it is available for general release._

## What's New

This version of `browser-switch` simplifies the API surface and allows for more flexibility with integration patterns by giving the integrating app control over storage and retrieval of the pending browser switch request. 

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

    override fun onResume() {
        handleReturnToAppFromBrowser(intent)
    }
    
    fun startBrowserSwitch() {
        val browserSwitchOptions = BrowserSwitchOptions().apply {
            requestCode = MY_REQUEST_CODE
            url = "https://example.com"
            returnUrlScheme = "my-custom-url-scheme"
        }
        when (val pendingRequest = browserSwitchClient.start(this, browserSwitchOptions)) {
            is BrowserSwitchPendingRequest.Started -> { 
                // store pending request
            }
            is BrowserSwitchPendingRequest.Failure -> { 
                // browser was unable to be launched, handle failure
            }
        }
    }
    
    fun handleReturnToAppFromBrowser(intent: Intent) {
        // fetch stored pending request
        storedPendingRequest()?.let { startedRequest ->
            val browserSwitchResult = browserSwitchClient.parseResult(startedRequest, intent)
            if (browserSwitchResult != null) {
                // handle successful browser switch result
                // clear stored pending request
            } else {
                // user did not complete browser switch
                // allow user to complete browser switch, or clear stored pending request
            }
        }
    }
}
```

## Launch Modes

If your deep link destination activity is configured in the `AndroidManifest.xml` with `android:launchMode="singleTop"`, `android:launchMode="singleTask"` or `android:launchMode="singleInstance"` handle the return to app from browser with the intent from `onNewIntent` rather than `onResume`

```kotlin
class MySingleTopActivity : ComponentActivity() {

    val browserSwitchClient: BrowserSwitchClient = BrowserSwitchClient()

    override fun onResume() {
        // do nothing
    }
    
    override fun onNewIntent(newIntent: Intent) {
        handleReturnToAppFromBrowser(newIntent)
    }    
}

```