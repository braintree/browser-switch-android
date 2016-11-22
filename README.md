# Android Browser Switch

Android Browser Switch makes it easy to open a url in a browser or
[Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) and receive a
response as the result of user interaction, either cancel or response data from the web page.

## Setup

Declare `BrowserSwitchActivity` in your `AndroidManifest.xml`:

```xml
<activity android:name="com.braintreepayments.browser_switch.BrowserSwitchActivity"
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
browserSwitchFragment.browserSwitch("http://example.com/");
// or
browserSwitchFragment.browserSwitch(intent);
```

The response will be returned in your implementation of `BrowserSwitchFragment#onBrowserSwitchResult`:

```java
@Override
public void onBrowserSwitchResult(BrowserSwitchResult result, @Nullable Uri returnUri) {
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
            // most likely cause by incorrect setup
            break;
    }
}
```
