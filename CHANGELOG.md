# browser-switch-android Release Notes

## TBD

* Use the browser switch with custom tabs in the same activity task as `BraintreeBrowserSwitch`s activity
* Breaking changes:
  * The activity which launches the browser switch must now have `android:launchMode="singleTask"`. See the `DemoActivity` entry in the [AndroidManifest.xml](demo/src/main/AndroidManifest.xml) file in the demo application
  * To ensure that the calling activity will receive the the browser switch result even if the OS kills the app process when the browser is open, you should indicate the activity on app startup. See [DemoApplication](demo/src/main/java/com/braintreepayments/browserswitch/demo/DemoApplication.java) in the demo application:
    ```java
    BrowserSwitchActivity.setReturnIntent(new Intent(this, DemoActivity.class))
    ```

## 1.1.3

* Update androidx dependencies to latest versions

## 1.1.2

* Update to SDK version 30
* Add `query` section to AndroidManifest to allow querying for web browsers on a device

## 1.1.1

* Fix bug where `getReturnUrlScheme` is called on BrowserSwitchFragment and an Activity is no longer attached to the fragment

## 1.1.0

* Create `BrowserSwitchOptions` value object for configuring browser switch behavior
* Add BrowserSwitchClient#start overloads with `BrowserSwitchOptions` param
* Add BrowserSwitchFragment#browserSwitch overload with `BrowserSwitchOptions` param

## 1.0.0

* Create BrowserSwitchClient to allow browser switch behavior through composition as well as inheritance.
* Update minSdkVersion from 15 to 21 (thanks! @calvarez-ov)
* Update Gradle build tools version to 3.6.3
* Breaking Changes
  * BrowserSwitchActivity::getReturnUri() has been removed
  * BrowserSwitchActivity::clearReturnUri() has been removed

## 0.2.0

* Using androidx 1.0.0
* Migrate Fragment to `androidx.fragment.app.Fragment`

## 0.1.6

* Update to SDK version 28

## 0.1.5

* Return an error when no activities are available to handle browser switch intent
* Update compile and target SDK versions to 26

## 0.1.4

* Prevent Chrome Custom Tab from being killed when user leaves
* Stop using dependency ranges

## 0.1.3

* Prevent dependency resolution of alpha major versions of support-annotations

## 0.1.2

* Include a message when an error is returned

## 0.1.1

* Change artifact id to `browser-switch` from `browser_switch`

## 0.1.0

* Initial release
