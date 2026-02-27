package com.wizzair.utils;

import com.wizzair.config.ConfigLoader;
import com.wizzair.config.DeviceConfig;
import com.wizzair.core.driver.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Opens the WizzAir app via a deep link URL.
 *
 * <p>Approach:
 * <ul>
 *   <li><b>Android:</b> Uses the ADB-equivalent {@code mobile: deepLink} execute script,
 *       which triggers the intent on the device without requiring ADB shell access.</li>
 *   <li><b>iOS:</b> Uses {@code mobile: launchApp} with {@code urlScheme}, which maps
 *       to XCUITest {@code openURL} under the hood.</li>
 * </ul>
 *
 * <p>Deep link format: {@code wizzair://flights/<origin>/<destination>/<date>}
 */
public final class DeepLinkHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DeepLinkHelper.class);

    private DeepLinkHelper() {}

    /**
     * Builds and opens a deep link that navigates to a specific flight.
     *
     * @param origin      IATA code, e.g. "LTN"
     * @param destination IATA code, e.g. "BCN"
     * @param date        ISO date, e.g. "2025-07-15"
     */
    public static void openFlight(String origin, String destination, String date) {
        DeviceConfig cfg = ConfigLoader.load();
        String url = String.format("%s://flights/%s/%s/%s",
                cfg.getDeepLinkScheme(), origin, destination, date);

        LOG.info("Opening deep link: {}", url);

        if (cfg.isAndroid()) {
            openDeepLinkAndroid(url, cfg.getAppPackage());
        } else {
            openDeepLinkIos(url);
        }
    }

    // -------------------------------------------------------------------------
    // Platform-specific
    // -------------------------------------------------------------------------

    private static void openDeepLinkAndroid(String url, String appPackage) {
        AndroidDriver driver = (AndroidDriver) DriverManager.getDriver();
        // Appium's mobile:deepLink script wraps am start -d <url>
        driver.executeScript("mobile: deepLink", Map.of(
            "url", url,
            "package", appPackage
        ));
    }

    private static void openDeepLinkIos(String url) {
        IOSDriver driver = (IOSDriver) DriverManager.getDriver();
        // XCUITest: open URL using Siri/Safari deep link resolution
        driver.executeScript("mobile: launchApp", Map.of(
            "bundleId", "com.apple.mobilesafari",
            "arguments", new String[]{}
        ));
        // Navigate Safari to the deep link – XCUITest will hand off to the app
        driver.get(url);
    }
}
