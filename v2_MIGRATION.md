# Android Browser Switch v2 (Beta) Migration Guide
 
See the [CHANGELOG](/CHANGELOG.md) for a complete list of changes. This migration guide outlines the basics for updating your browser switch integration from v1 to v2.

_Documentation for v2 will be published to https://developers.braintreepayments.com once it is available for general release._

## Setup

Add the library to your dependencies in your `build.gradle`:

```groovy
dependencies {
  implementation 'com.braintreepayments.api:browser-switch:2.0.0-beta1'
}
```

Then, add an `intent-filter` in the `AndroidManifest.xml` to your deep link destination activity:

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

**Note**: The scheme you define must use all lowercase letters. This is due to [scheme matching on the Android framework being case sensitive, expecting lower case](https://developer.android.com/guide/topics/manifest/data-element#scheme).

If these requirements are not met, an exception will be thrown in the `BrowserSwitchClient#start` method and no browser switch will occur.

## Usage

```java
package com.company.app;

public class MyBrowserSwitchActivity extends AppCompatActivity {

  private static final int MY_REQUEST_CODE = 123;

  private BrowserSwitchClient browserSwitchClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...

    browserSwitchClient = new BrowserSwitchClient();
  }

  @Override
  protected void onResume() {
    super.onResume();
    ...

    // call 'deliverResult' in onResume to capture a pending browser switch result
    BrowserSwitchResult result = browserSwitchClient.deliverResult(this);
    if (result != null) {
      myHandleBrowserSwitchResultMethod(result); 
    }
  }
  
  public void myStartBrowserSwitchMethod() { 
    BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
            .requestCode(MY_REQUEST_CODE)
            .url("https://example.com")
            .returnUrlScheme("my-custom-url-scheme");

    browserSwitchClient.start(this, browserSwitchOptions);
  }   

  private void myHandleBrowserSwitchResultMethod(BrowserSwitchResult browserSwitchResult) {
    int statusCode = browserSwitchResult.getStatus();
    switch (statusCode) {
      case BrowserSwitchStatus.SUCCESS:
        // handle success
        break;
      case BrowserSwitchStatus.CANCELED:
        // handle cancel
        break;
     }
  }   
}
```

## Launch Modes

If your deep link destination activity is configured in the `AndroidManifest.xml` with `android:launchMode="singleTop"`, 
`android:launchMode="singleTask"` or `android:launchMode="singleInstance"` add the following code snippet:


```java
package com.company.app;

public class MyBrowserSwitchActivity extends AppCompatActivity {
  ... 

  @Override
  protected void onNewIntent(Intent newIntent) {
    super.onNewIntent(newIntent);
    setIntent(newIntent);
  }
}
```
