# Browser Switch Integration Comparison

## Existing Pattern

A merchant creates a `BrowserSwitchClient` to interact with the SDk. The merchant
calls `browserSwitchClient#start` to launch a URL in an external web browser. Before launching the
URL, the browser switch SDK uses shared preferences (on-device storage) to store a pending request.
The SDK creates an starts a Chrome Custom Tabs intent. After some interaction within that launched
website, the user is deep-linked back into the calling merchant app. When the merchant app is
resumed (`onResume`) the merchant app calls`browserSwitchClient#parseResult`. The browser switch
library checks shared preferences for a pending request and matches it with the result parsed from
the deep link used to return to the app. If a matching request/result exists, it is delivered to the
merchant app. After receiving/handling a result, the merchant app
calls `browserSwitchClient#clearActiveRequests` to tell the browser switch library to clear shared
preferences of the pending request.

If a user cancels the browser switch (closes the browser without completing), or navigates away from
the browser and back to the app, the merchant app will not receive a result from the SDK. They are
responsible for including their own logic to determine if the app is resumed (`onResume` invoked),
after a browser switch was made, and before it was completed. If the user navigates away from the
browser and back to the merchant app, it would still be possible to return to the browser and
complete the browser switch flow, unless the merchant app logic prevents it.

### Pros

1. Un-opinionated integration that leaves it up to the merchant app to handle cancel scenarios
2. Resistant to process kills (results will be delivered as expected even after process kill)
3. Doesn't require the Activity Result API

### Cons

1. Doesn't deliver any result for cancellation/incomplete flows
2. May deliver incorrect result if the same activity is used to launch browser switch more than
   once. Specifically the scenario of a successful browser switch flow, followed by a browser switch
   flow in which the user cancels (the first success result with be re-delivered).

## Proposed Pattern

A merchant creates a `BrowserSwitchLauncher` to interact with the SDK. The `BrowserSwitchLauncher`
defines an Activity Result Contract using the Android Activity Result API to launch a browser (
Custom Tabs) activity, and parse a result. A merchant calls `browserSwitchLauncher#launch` to launch
a URL in an external web browser. Before launching the URL, the browser switch SDK uses shared
preferences (on-device storage) to store a pending request. The SDK launches a browser based on the
intent defined in the Activity Result Contract. After some interaction within that launched website,
the user is deep-linked back into the calling merchant app. When the merchant app is
resumed (`onResume`) the merchant app calls `browserSwitchLauncher#handleReturnToAppFromBrowser`.
The browser switch library checks shared preferences for a pending request and matches it with the
result parsed from the deep link used to return to the app. The browser switch library also checks
if the activity launched via the Activity Result API (the browser) was finished, to deliver a
browser switch incomplete result. If a matching request/result exists, it is delivered to the
merchant app, and shared preferences are cleared of the pending request.

If a user cancels the browser switch (closes the browser without completing), or navigates away from
the browser and back to the app, the merchant app will receive a result with status `INCOMPLETE`.
The SDK cannot differentiate between a user closing the browser (explicitly cancelling the browser
switch flow), or simply navigating away from the browser and returning to the merchant app (perhaps
with the intent of completing the flow). In both of these cases, the merchant app will receive
an `INCOMPLETE` result, and browser switch would need to be re-launched in order to complete the
flow (the user cannot simply return to the browser and complete the flow).

## Pros

1. Delivers a result in the cancel/incomplete flow scenario
2. Resistant to process kills (Con scenario 2 from above still exists in process kill scenario)
3. Mostly resolves incorrect result (Con scenario 2) from above

## Cons

1. Uses Activity Result API which requires that `BrowserSwitchLauncher` be instantiated
   before `onCreate` in a merchant app
2. Adds additional state management/opinion to the SDK which may not be as flexible for merchant
   integrations long-term

## Alternate Options

Open to suggestions! There doesn't seem to be a clear alternative that allows for handling
successful deep link cases and cancel scenarios. From my understanding, WebViews are not an
alternative due to security concerns.
