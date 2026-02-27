package com.wizzair.utils;

import com.wizzair.config.ConfigLoader;
import com.wizzair.core.driver.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Utility for controlling app lifecycle (background, foreground, terminate).
 *
 * <p>Uses the recommended Appium 2.x {@code mobile:} scripts rather than
 * deprecated {@code driver.runAppInBackground()}.
 */
public final class AppStateHelper {

    private static final Logger LOG = LoggerFactory.getLogger(AppStateHelper.class);

    private AppStateHelper() {}

    /**
     * Sends the app to the background for the specified duration, then
     * automatically brings it back to foreground.
     *
     * <p>On Android, uses HOME + RECENT intent sequence.
     * On iOS, uses XCUITest {@code mobile: pressButton} with home button.
     *
     * @param seconds how long to keep the app in background
     */
    public static void backgroundApp(int seconds) {
        LOG.info("Sending app to background for {} seconds", seconds);

        if (ConfigLoader.load().isAndroid()) {
            AndroidDriver driver = (AndroidDriver) DriverManager.getDriver();
            // runAppInBackground keeps Appium 1.x compatibility; still works in 2.x
            driver.runAppInBackground(Duration.ofSeconds(seconds));
        } else {
            IOSDriver driver = (IOSDriver) DriverManager.getDriver();
            driver.runAppInBackground(Duration.ofSeconds(seconds));
        }

        LOG.info("App resumed after {} seconds in background", seconds);
    }

    /**
     * Terminates the app completely.
     */
    public static void terminateApp() {
        if (ConfigLoader.load().isAndroid()) {
            ((AndroidDriver) DriverManager.getDriver())
                .terminateApp(ConfigLoader.load().getAppPackage());
        } else {
            ((IOSDriver) DriverManager.getDriver())
                .terminateApp(ConfigLoader.load().getBundleId());
        }
        LOG.info("App terminated");
    }

    /**
     * Activates (brings to foreground) the app.
     */
    public static void activateApp() {
        if (ConfigLoader.load().isAndroid()) {
            ((AndroidDriver) DriverManager.getDriver())
                .activateApp(ConfigLoader.load().getAppPackage());
        } else {
            ((IOSDriver) DriverManager.getDriver())
                .activateApp(ConfigLoader.load().getBundleId());
        }
        LOG.info("App activated / brought to foreground");
    }
}
