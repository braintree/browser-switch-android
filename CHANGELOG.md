# browser-switch-android Release Notes

## unreleased

* Add BrowserSwitchClient#getResult() method to peek at a pending browser switch result before it is delivered
* Add BrowserSwitchClient#getResultFromCache() method to peek at a cached browser switch result before it is delivered

## 2.2.0

* Add BrowserSwitchClient#captureResult() method to capture a browser switch result into persistent storage
* Add BrowserSwitchClient#deliverResultFromCache() method to deliver a previously captured browser switch result 

## 2.1.1

* Fallback to browser when Chrome Custom Tabs is unavailable (thanks! @calvarez-ov)

## 2.1.0

* Upgrade `compileSdkVersion` and `targetSdkVersion` to API 31

## 1.2.0

* Upgrade `compileSdkVersion` and `targetSdkVersion` to API 31

## 2.0.2

* Add internal methods for usage with other Braintree libraries.

## 2.0.1

* Update `BrowserSwitchClient#deliverResult()` to allow canceled browser switches a chance to reach the success state (See braintree_android [#409](https://github.com/braintree/braintree_android/issues/409))
* Fix nullability annotations in `BrowserSwitchOptions` setters 

## 2.0.0

* Includes all changes in [2.0.0-beta1](#200-beta1), [2.0.0-beta2](#200-beta2), and [2.0.0-beta3](#200-beta3)

## 2.0.0-beta3

* Fix issue where app links couldn't be opened by `BrowserSwitchClient#start()`

## 1.1.4

* Fix issue where app links couldn't be opened by `BrowserSwitchClient#start()`

## 2.0.0-beta2

* Fix issue of false successful result when `deepLinkUrl` did not match request `returnUrlScheme`

## 2.0.0-beta1

* Add `BrowserSwitchException`
* Add `returnUrlScheme` to `BrowserSwitchOptions`
* Add `requestCode`, `requestUrl` and `deepLinkUrl` properties to `BrowserSwitchResult`
* Breaking Changes
  * Move BrowserSwitch module from `com.braintreepayments.browserswitch` package to `com.braintreepayments.api`
  * Remove `BrowserSwitchFragment`
  * Remove `BrowserSwitchActivity`
  * Remove `BrowserSwitchClient` static constructor
  * Remove convenience `BrowserSwitchClient#start` methods
  * Remove convenience `BrowserSwitchClient#deliverResult` methods
  * Remove `STATUS_ERROR` and throw error in `start` method
  * Remove `BrowserSwitchResult#getErrorMessages`
  * Remove support for `Intent` on `BrowserSwitchOptions`
  * Remove `ChromeCustomTabs`
  * Rename `BrowserSwitchResult.STATUS_OK` to `BrowserSwitchStatus.SUCCESS` 
  * Rename `BrowserSwitchResult.STATUS_CANCELED` to `BrowserSwitchStatus.CANCELED` 
  * Remove `BrowserSwitchListener`

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
