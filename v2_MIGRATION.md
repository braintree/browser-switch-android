# Browser Switch Android v2 (Beta) Migration Guide
 
See the [CHANGELOG](/CHANGELOG.md) for a complete list of changes. This migration guide outlines the basics for updating your client integration from v1 to v2.

_Documentation for v5 will be published to https://developers.braintreepayments.com once it is available for general release._

## Setup

Add the library to your dependencies in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:browser-switch:2.0.0-beta1'
}
```

Then, add an `intent-filter` in the `AndroidManifest.xml` to configure your activity to handle deep links:

```xml
<activity android:name="com.company.app.MyBrowserSwitchActivity">
    ...
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <data android:scheme="my-custom-url-scheme"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
    </intent-filter>
</activity>
```

**Note**: The scheme you define must use all lowercase letters. This is due to [scheme matching on the Android framework being case sensitive, expecting lower case](https://developer.android.com/guide/topics/manifest/data-element#scheme). If your package contains underscores, the underscores should be removed when specifying the scheme.

If these requirements are not met, an exception will be thrown in the `BrowserSwitchClient#start` method and no browser switch will occur.

## Usage


```java
package com.company.app;
...

public class MyBrowserSwitchActivity extends AppCompatActivity {

  private BrowserSwitchClient browserSwitchClient;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...

    browserSwitchClient = new BrowserSwitchClient();
    browserSwitchClient.start();
  }

  @Override
  public void onResume() {
    super.onResume();
    ...

    // call 'deliverResult' in onResume to ensure that all pending
    // browser switch results are delivered to the listener
    browserSwitchClient.deliverResult();
  }
}
```

