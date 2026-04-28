# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew build                        # Full build with tests
./gradlew test                         # Unit tests (Robolectric, no device needed)
./gradlew test --tests "*ClassName*"   # Run a single test class
./gradlew check                        # All checks including detekt lint
./gradlew detekt                       # Code quality analysis only
./gradlew publishToMavenLocal          # Publish to local Maven for integration testing
./gradlew dokkaHtmlMultiModule         # Generate API documentation
scripts/start-local-development.sh     # Continuous build + local Maven publish
```

## Architecture

This is an Android SDK library (`browser-switch`) with a companion demo app (`demo`). It enables apps to open URLs in Chrome Custom Tabs or Auth Tabs and receive deep-link callback results.

**Modules**: `:browser-switch` (library, published to Maven Central), `:demo` (Jetpack Compose demo app)

### Core Flow

1. App calls `BrowserSwitchClient.start()` with a URL and return URL scheme → library validates device capability, launches browser
2. Browser redirects back to app's custom scheme → Android fires deep-link intent
3. App calls `BrowserSwitchClient.completeRequest()` with stored pending request + intent → returns sealed result

State persistence is the **app's responsibility**: `BrowserSwitchStartResult.Started` carries a Base64-encoded JSON string the app must store and restore across process death.

### Key Classes

- **`BrowserSwitchClient`** — Public API. Three constructors: no-arg (Custom Tabs only), `ActivityResultCaller` (Auth Tab for Activities), `ActivityResultRegistry` (Auth Tab for Compose). Methods: `start()` → `BrowserSwitchStartResult`, `completeRequest()` → `BrowserSwitchFinalResult`, `restorePendingRequest()`.
- **`BrowserSwitchOptions`** — Builder for URL, request code, metadata, return URL scheme, app link URI, and `LaunchType`.
- **`BrowserSwitchStartResult`** — Sealed Kotlin class: `Started(pendingRequest: String)` | `Failure(error: Exception)`
- **`BrowserSwitchFinalResult`** — Sealed Kotlin class: `Success` | `Failure` | `NoResult`
- **`AuthTabInternalClient`** — Internal. Selects Auth Tab (Chrome 137+) or falls back to Custom Tabs; requires `ActivityResultLauncher` set up in constructor.
- **`BrowserSwitchInspector`** — Internal. Validates device can handle deep links before launch.
- **`BrowserSwitchRequest`** — Internal. Serializes/deserializes request state to Base64-encoded JSON.
- **`LaunchType`** — Enum: `ACTIVITY_NEW_TASK` | `ACTIVITY_CLEAR_TOP`

### Testing

Tests use **Robolectric** (no emulator required), JUnit 4, Mockito, and MockK. All tests live in `browser-switch/src/test/java/`. `BrowserSwitchClientUnitTest.java` is the primary test file covering the main client behavior.

### Publishing

Published to Maven Central via Sonatype OSSRH. Requires env vars `SONATYPE_NEXUS_USERNAME`, `SONATYPE_NEXUS_PASSWORD`, and signing keys (`SIGNING_KEY_ID`, `SIGNING_KEY_PASSWORD`, `SIGNING_KEY_FILE`). Release tasks: `changeGradleReleaseVersion`, `updateCHANGELOGVersion`, `incrementSNAPSHOTVersion`.

### Versions

- compileSdk 36, minSdk 23, Java 11, Kotlin 1.9.20
- Current: `3.5.2-SNAPSHOT`; v2.x deprecated March 2026
