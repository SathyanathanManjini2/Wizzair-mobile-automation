# WizzAir Mobile Automation Framework

> Appium 9 · Cucumber 7 · Java 17 · JUnit 5 · Allure Reports

A production-grade BDD mobile automation framework that covers Android **and** iOS, targeting the WizzAir flight booking app. All five required scenarios are implemented, with emphasis on stability, cross-platform support, and clean architecture.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Setup](#setup)
4. [Running the Tests](#running-the-tests)
5. [Design Decisions](#design-decisions)
6. [Locator Strategy](#locator-strategy)
7. [Synchronisation Strategy](#synchronisation-strategy)
8. [Cross-Platform Handling](#cross-platform-handling)
9. [Assumptions & Limitations](#assumptions--limitations)
10. [Viewing Reports](#viewing-reports)

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 17+ | `JAVA_HOME` must be set |
| Maven | 3.9+ | |
| Node.js | 18+ | Required by Appium |
| Appium Server | 2.x | `npm install -g appium` |
| Android SDK | API 30+ | `ANDROID_HOME` must be set |
| Xcode | 15+ | macOS only, for iOS |
| UiAutomator2 driver | latest | `appium driver install uiautomator2` |
| XCUITest driver | latest | `appium driver install xcuitest` |

---

## Project Structure

```
wizzair-appium/
├── pom.xml
└── src/test/
    ├── java/com/wizzair/
    │   ├── TestRunner.java                  # JUnit Platform Suite entry point
    │   ├── config/
    │   │   ├── ConfigLoader.java            # Reads YAML + system-property overrides
    │   │   └── DeviceConfig.java            # POJO for device/environment settings
    │   ├── core/
    │   │   ├── driver/
    │   │   │   ├── DriverFactory.java       # Creates Android/iOS drivers
    │   │   │   └── DriverManager.java       # Thread-local driver holder
    │   │   ├── wait/
    │   │   │   └── WaitStrategy.java        # All waiting utilities (no Thread.sleep)
    │   │   └── context/
    │   │       └── ContextManager.java      # Native ↔ WebView context switching
    │   ├── pages/common/
    │   │   ├── BasePage.java                # Parent for all page objects
    │   │   ├── PermissionHandler.java       # OS-level permission dialogs
    │   │   ├── FlightSearchPage.java        # Home / search screen
    │   │   ├── FlightResultsPage.java       # Results list with infinite scroll
    │   │   ├── FlightDetailsPage.java       # Deep-link target screen
    │   │   ├── BookingPage.java             # Passenger details + price modal
    │   │   └── PaymentPage.java             # WebView payment form
    │   ├── steps/
    │   │   ├── DeepLinkSteps.java           # Scenario 1 step defs
    │   │   ├── FlightSearchSteps.java       # Scenario 2 step defs
    │   │   ├── PriceChangeSteps.java        # Scenario 3 step defs
    │   │   ├── PaymentSteps.java            # Scenario 4 step defs
    │   │   └── BackgroundResumeSteps.java   # Scenario 5 step defs
    │   ├── hooks/
    │   │   └── DriverHooks.java             # Before/After hooks; driver lifecycle
    │   └── utils/
    │       ├── DeepLinkHelper.java          # Opens deep links on Android & iOS
    │       ├── ScrollHelper.java            # W3C gesture-based scrolling
    │       ├── AppStateHelper.java          # Background / resume / terminate
    │       └── ScreenshotHelper.java        # Allure screenshot attachments
    └── resources/
        ├── configs/
        │   ├── android-config.yaml          # Android device/emulator settings
        │   └── ios-config.yaml              # iOS device/simulator settings
        ├── features/
        │   ├── 01_deep_link_permissions.feature
        │   ├── 02_flight_search_infinite_scroll.feature
        │   ├── 03_price_change_modal.feature
        │   ├── 04_payment_webview.feature
        │   └── 05_background_resume.feature
        ├── cucumber.properties
        └── logback-test.xml
```

---

## Setup

### 1. Clone and install dependencies

```bash
git clone <your-repo-url>
cd wizzair-appium
mvn dependency:resolve
```

### 2. Configure your device

Edit `src/test/resources/configs/android-config.yaml` **or** `ios-config.yaml`:

```yaml
# Minimum required fields:
deviceName:    "Pixel_7_API_33"   # AVD name or real device name
platformVersion: "13.0"
appPackage:    "com.wizzair.WizzAirApp"
appActivity:   ".activity.MainActivity"
appiumServerUrl: "http://127.0.0.1:4723"
```

### 3. Start Appium Server

```bash
appium --port 4723
```

### 4. (Optional) Install the WizzAir APK/IPA

```yaml
# In config YAML:
appPath: "/absolute/path/to/wizzair.apk"
```

If `appPath` is empty, the framework assumes the app is already installed.

---

## Running the Tests

### All tests (Android)

```bash
mvn test -Dplatform=android
```

### All tests (iOS)

```bash
mvn test -Dplatform=ios
```

### Only smoke tests

```bash
mvn test -Dplatform=android -Dcucumber.filter.tags="@smoke"
```

### Single scenario (by tag)

```bash
mvn test -Dplatform=android -Dcucumber.filter.tags="@deep-link"
mvn test -Dplatform=android -Dcucumber.filter.tags="@payment"
mvn test -Dplatform=android -Dcucumber.filter.tags="@background-resume"
```

### Override any config value on the command line

```bash
mvn test -Dplatform=android \
         -DdeviceName="emulator-5554" \
         -Dudid="RF8M123ABCD" \
         -DappiumServerUrl="http://192.168.1.10:4723"
```

---

## Design Decisions

### 1. Page Object Model (POM) with BasePage

Every screen is modelled as a class extending `BasePage`. BasePage:
- Initialises `AppiumFieldDecorator` for `@AndroidFindBy` / `@iOSXCUITFindBy` annotations.
- Exposes protected `tap()`, `type()`, `getText()` helpers so page classes stay concise.
- Enforces an `isLoaded()` contract so any page can verify its own readiness.

### 2. Separation of concerns via layers

```
Feature files → Step Definitions → Page Objects → Core (Driver / Wait / Context)
```

Step definitions never touch the driver directly – they only call page object methods. Page objects never know about Cucumber.

### 3. ThreadLocal DriverManager

`DriverManager` stores the driver in a `ThreadLocal`, enabling safe parallel execution in the future with minimal refactoring.

### 4. Strongly-typed Capabilities

`UiAutomator2Options` and `XCUITestOptions` are used instead of raw `DesiredCapabilities`, giving compile-time validation of capability names.

### 5. Awaitility for non-element waits

Element conditions use Selenium's `WebDriverWait`; non-element conditions (e.g. "wait for WebView context to appear", "wait for modal") use **Awaitility**, which provides cleaner syntax and better error messages.

### 6. W3C Gestures for scrolling

`TouchAction` (deprecated in Appium 2) is replaced with W3C `PointerInput` sequences. Android additionally uses UiAutomator2's `UiScrollable` for text-based scrolling when available.

---

## Locator Strategy

Priority (most stable → least stable):

| Priority | Strategy | Example |
|----------|----------|---------|
| 1st | `accessibility id` | `@AndroidFindBy(accessibility = "Search flights")` |
| 2nd | `resource-id` (Android) / `name` (iOS) | `@AndroidFindBy(id = "com.wizzair:id/btn_search")` |
| 3rd | CSS selector (WebView only) | `input[data-cy='card-number']` |
| 4th | XPath (last resort) | `//android.widget.Button[@text='Allow']` |

**Avoided**: XPath based on visual hierarchy, index, or sibling position.

**`data-cy` attributes** are used for WebView elements – these are testing-specific attributes that the development team should add to the embedded web forms. This is a common practice in hybrid apps.

---

## Synchronisation Strategy

| Situation | Mechanism |
|-----------|-----------|
| Element visibility / clickability | `WebDriverWait` + `ExpectedConditions` |
| Loading spinner disappearance | `waitForInvisibility()` |
| Non-element conditions (context, modals) | `Awaitility.await()` |
| Deep link navigation | Implicit wait via `PageFactory` (15s) + explicit `isLoaded()` |
| WebView appearance | `ContextManager.switchToWebView(30)` with Awaitility polling |

**Zero `Thread.sleep()` calls** exist in the framework.

---

## Cross-Platform Handling

| Challenge | Solution |
|-----------|----------|
| Different locators per platform | `@AndroidFindBy` + `@iOSXCUITFindBy` annotations on same field |
| Different driver types | `DriverFactory` creates the correct subtype; callers use `AppiumDriver` |
| Deep links | `DeepLinkHelper` uses `mobile: deepLink` (Android) vs Safari handoff (iOS) |
| Permissions | `PermissionHandler` checks Android button texts / iOS XCUITest alert buttons |
| Background/resume | `AppStateHelper.backgroundApp()` calls `runAppInBackground()` on both platforms |
| Scrolling | `ScrollHelper` uses W3C PointerInput (cross-platform) + UiAutomator2 shortcut (Android) |
| WebView context | `ContextManager` waits for `WEBVIEW_*` prefix context on both platforms |

---

## Assumptions & Limitations

### Assumptions

1. **App is already installed** on the device when `appPath` is not set.
2. **Appium Server 2.x** is running locally on port 4723 (configurable).
3. **Test environment** supports triggering the price-change modal (Scenario 3). In production, the modal is non-deterministic; for reliable CI tests a backend test flag or mock server is required.
4. **WebView debugging** is enabled in the app's WebView (`setWebContentsDebuggingEnabled(true)`) – required for Appium to discover the WebView context.
5. **Payment form** `data-cy` attributes are present. If not, CSS selectors in `PaymentPage` must be updated to match actual attributes.
6. **Accessibility IDs** in the locators (e.g. `"Search flights"`, `"Origin airport"`) match actual values in the running app. These must be verified/updated against the real app's accessibility tree using Appium Inspector.

### Limitations

1. **No real APK/IPA provided** – locators are based on expected accessibility IDs and must be validated/adjusted using Appium Inspector against the real app.
2. **Price change modal (Scenario 3)** cannot be deterministically triggered without backend cooperation or a mock layer.
3. **iOS deep link via Safari** may require additional configuration on real devices (trust store, WebDriverAgent signing).
4. **Parallel execution** is configured `false` by default – enabling it requires separate device/emulator instances per thread.
5. **Dynamic locators** in `FlightResultsPage` (flight time matching) assume a specific accessibility label format. Verify with Appium Inspector.

---

## Viewing Reports

### Allure Report (recommended)

```bash
mvn allure:serve
```

Or generate static HTML:

```bash
mvn allure:report
open target/site/allure-maven-plugin/index.html
```

### Plain Cucumber HTML

```
target/cucumber-reports/cucumber.html
```

### Logs

```
target/logs/test-run.log
```

---

## Tag Reference

| Tag | Description |
|-----|-------------|
| `@smoke` | Fast happy-path tests |
| `@deep-link` | Scenario 1 |
| `@infinite-scroll` | Scenario 2 |
| `@price-change` | Scenario 3 |
| `@webview` / `@e2e` | Scenario 4 |
| `@background-resume` / `@resilience` | Scenario 5 |
| `@android` | Android only |
| `@ios` | iOS only |
| `@wip` | Skipped by default runner |
