# Android Browser Switch

[![Build Status](https://travis-ci.org/braintree/browser-switch-android.svg?branch=master)](https://travis-ci.org/braintree/browser-switch-android)

Android Browser Switch makes it easy to open a url in a browser or
[Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) and receive a
response as the result of user interaction, either cancel or response data from the web page.

## Setup

Add the library to your dependencies in your `build.gradle`:

```groovy
dependencies {
  compile 'com.braintreepayments:browser-switch:0.1.2'
}
```

Declare `BrowserSwitchActivity` in your `AndroidManifest.xml`:

```xml
<activity android:name="com.braintreepayments.browserswitch.BrowserSwitchActivity"
    android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <data android:scheme="${applicationId}.browserswitch"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
    </intent-filter>
</activity>
```

Your app's url scheme must begin with your app's package and end with `.browserswitch`.
For example, if the package is `com.your-company.your-app`, then your url scheme should be
`com.your-company.your-app.browserswitch`.

`${applicationId}` is automatically applied with your app's package when using Gradle.

The scheme you define must use all lowercase letters. If your package contains underscores,
the underscores should be removed when specifying the scheme.

If these requirements are not met, an error will be returned and no browser switch will occur.

## Usage

`BrowserSwitchFragment` is an abstract `Fragment` that should be extended and used to start and
handle the response from a browser switch.

The url scheme to use to return to your app can be retrieved using:

```java
browserSwitchFragment.getReturnUrlScheme();
```

This scheme should be used to build a return url and passed along in the browser switch to the
target web page. This url can be loaded to return to the app from the target web page.

A browser switch can be initiated by calling:

```java
browserSwitchFragment.browserSwitch(requestCode, "http://example.com/");
// or
browserSwitchFragment.browserSwitch(requestCode, intent);
```

The response will be returned in your implementation of `BrowserSwitchFragment#onBrowserSwitchResult`:

```java
@Override
public void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri) {
    switch (result) {
        case OK:
            // the browser switch returned data in the return uri
            break;
        case CANCELED:
            // the user canceled and returned to your app
            // return uri is null
            break;
        case ERROR:
            // there was an error browser switching
            // most likely caused by incorrect setup
            break;
    }
}
```

## License

Android Browser Switch is open source and available under the MIT license. See the
[LICENSE](LICENSE) file for more info.
