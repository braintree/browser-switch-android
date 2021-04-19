# Android Browser Switch

[![Build Status](https://travis-ci.org/braintree/browser-switch-android.svg?branch=master)](https://travis-ci.org/braintree/browser-switch-android)

Android Browser Switch makes it easy to open a url in a browser or
[Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) and receive a
response as the result of user interaction, either cancel or response data from the web page.

:mega:&nbsp;&nbsp;A new major version of the SDK is available in beta. See the [v2 migration guide](v2_MIGRATION.md) for details.

## Setup

Add the library to your dependencies in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments:browser-switch:1.1.4'
}
```

Then, declare `BrowserSwitchActivity` in your `AndroidManifest.xml`:

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

**Note**: The scheme you define must use all lowercase letters. This is due to [scheme matching on the Android framework being case sensitive, expecting lower case](https://developer.android.com/guide/topics/manifest/data-element#scheme). If your package contains underscores, the underscores should be removed when specifying the scheme.

If these requirements are not met, an error will be returned and no browser switch will occur.

## Usage

`BrowserSwitchFragment` is an abstract `androidx.fragment.app.Fragment` that should be extended and used to start and
handle the response from a browser switch.

**Note**: The `Activity` that `BrowserSwitchFragment` attaches to cannot have a launch mode of `singleInstance`. `BrowserSwitchFragment` needs access to the calling `Activity` to provide a result and cannot do so if the browser switch happens on a different activity stack.

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
            // Some possible issues you may encounter are:
            // - There are no activities installed that can handle a URL.
            // - The integration is not setup correctly.
            break;
    }
}
```

## Alternative Usage: BrowserSwitchClient

For more fine-grained control over browser switching, `BrowserSwitchClient` can be used in scenarios where a custom `BrowserSwitchListener` is preferred. 

```java
public class CustomFragment extends Fragment {

  private BrowserSwitchClient browserSwitchClient;
  private BrowserSwitchListener browserSwitchListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...

    browserSwitchListener = new BrowserSwitchListener() {
      @Override
      public void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri) {
        // custom listener logic goes here
      }
    }; 

    browserSwitchClient = BrowserSwitchClient.newInstance();
    browserSwitchClient.start(requestCode, Uri.parse("http://example.com/"), this, browserSwitchListener);
  }

  @Override
  public void onResume() {
    super.onResume();
    ...

    // call 'deliverResult' in onResume to ensure that all pending
    // browser switch results are delivered to the listener
    browserSwitchClient.deliverResult(this, browserSwitchListener);
  }
}
```

## Versions

This SDK abides by our Client SDK Deprecation Policy. For more information on the potential statuses of an SDK check our [developer docs](http://developers.braintreepayments.com/guides/client-sdk/deprecation-policy).

| Major version number | Status | Released | Deprecated | Unsupported |
| -------------------- | ------ | -------- | ---------- | ----------- |
| 2.x.x | Beta | February 2021 | TBA | TBA |
| 1.x.x | Active | June 2020 | TBA | TBA |

## License

Android Browser Switch is open source and available under the MIT license. See the
[LICENSE](LICENSE) file for more info.
