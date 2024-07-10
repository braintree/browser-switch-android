# Android Browser Switch

![GitHub Actions Tests](https://github.com/braintree/browser-switch-android/workflows/Tests/badge.svg)

Android Browser Switch makes it easy to open a url in a browser or
[Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) and receive a
response as the result of user interaction, either cancel or response data from the web page.

## Setup

Add the library to your dependencies in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:browser-switch:3.0.0-beta1'
}
```

To preview the latest work in progress builds, add the following SNAPSHOT dependency in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:browser-switch:3.0.0-beta1-SNAPSHOT'
}
```

You will also need to add the Sonatype snapshots repo to your top-level `build.gradle` to import SNAPSHOT builds:

```groovy
allprojects {
    repositories {
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
    }
}
```

## AndroidManifest.xml

Declare an activity that you own as a deep link target in your `AndroidManifest.xml`:

```xml
<activity android:name="com.myapp.MyDeepLinkTargetActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <data android:scheme="my-custom-url-scheme"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
    </intent-filter>
</activity>
```

**Note**: The scheme you define must use all lowercase letters. This is due to [scheme matching on the Android framework being case sensitive, expecting lower case](https://developer.android.com/guide/topics/manifest/data-element#scheme). The scheme must also be [valid according to RFC 2396](https://datatracker.ietf.org/doc/html/rfc2396#section-3.1).

If these requirements are not met, an error will be returned and no browser switch will occur.

## Usage

A browser switch can be initiated by calling `BrowserSwitchClient#start()`. Use `BrowserSwitchOptions` to configure options for browser switching:

```kotlin
val browserSwitchOptions = BrowserSwitchOptions().apply {
    requestCode = MY_REQUEST_CODE
    url = "https://site-to-load.com?callbackURL=my-custom-url-scheme%3A%2F%2Fsuccess"
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
```

In the above example, notice the encoded `callbackURL` parameter is forwarded to the website that will be loaded. The callback url must have the same custom scheme set in `BrowserSwitchOptions`. When this URL is loaded by the site, the Android OS will re-direct the user to the deep link destination `Activity` defined in the `AndroidManifest.xml`.

To capture a browser switch result, override your deep link target `Activity` with the following code snippet:

```kotlin
override fun onResume() {
    handleReturnToAppFromBrowser(intent) 
}

fun handleReturnToAppFromBrowser(intent: Intent) {
    // fetch stored pending request
    fetchPendingRequestFromPersistentStorage()?.let { startedRequest ->
        when (val browserSwitchResult = browserSwitchClient.parseResult(startedRequest, intent)) {
            is BrowserSwitchResult.Success -> {
                // handle successful browser switch result
                // clear stored pending request
            }
            is BrowserSwitchResult.NoResult -> {
                // user did not complete browser switch
                // allow user to complete browser switch, or clear stored pending request
            }
        }
    }
}
```

## Launch Modes

If your deep link target `Activity` has `android:launchMode="singleTop"`, `android:launchMode="singleTask"`, or `android:launchMode="singleInstance"`, add the following code snippet to your deep link target `Activity` in the `onNewIntent` method instead of `onResume`:

```kotlin
override fun onNewIntent(newIntent: Intent?) {
    super.onNewIntent(intent)
    handleReturnToAppFromBrowser(intent) 
}
```

## Versions

This SDK abides by our Client SDK Deprecation Policy. For more information on the potential statuses of an SDK check our [developer docs](https://developer.paypal.com/braintree/docs/guides/client-sdk/deprecation-policy/android/v4).

| Major version number | Status   | Released      | Deprecated | Unsupported |
|----------------------|----------|---------------| ---------- | ----------- |
| 3.x.x                | Beta     | TBA           | TBA | TBA |
| 2.x.x                | Active   | February 2021 | TBA | TBA |
| 1.x.x                | Inactive | June 2020     | April 2022 | April 2023 |

## Help

* [Check out the reference docs](https://braintree.github.io/browser-switch-android/index.html)
* Find a bug? [Open an issue](https://github.com/braintree/browser-switch-android/issues)
* Want to contribute? [Check out contributing guidelines](CONTRIBUTING.md) and [submit a pull request](https://help.github.com/articles/creating-a-pull-request).

## License

Android Browser Switch is open source and available under the MIT license. See the
[LICENSE](LICENSE) file for more info.
