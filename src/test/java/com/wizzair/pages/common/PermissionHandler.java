package com.wizzair.pages.common;

import com.wizzair.config.ConfigLoader;
import com.wizzair.core.driver.DriverManager;
import com.wizzair.core.wait.WaitStrategy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handles system-level permission dialogs that appear on first launch.
 *
 * <p>Strategy:
 * <ul>
 *   <li><b>Android:</b> Targets the UiAutomator2 button text "Allow", "Only this time", etc.</li>
 *   <li><b>iOS:</b>  Targets the XCUITest native alert buttons.</li>
 * </ul>
 *
 * <p>Called from {@link com.wizzair.hooks.DriverHooks} after driver creation, and also
 * from scenario steps when permissions appear mid-flow.
 */
public final class PermissionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionHandler.class);

    // Android allow button resource ids / text variants
    private static final List<String> ANDROID_ALLOW_TEXTS = List.of(
            "Allow", "Allow all the time", "Only this time", "While using the app",
            "Allow only while using the app", "OK", "GOT IT"
    );

    // iOS alert accept button texts
    private static final List<String> IOS_ALLOW_TEXTS = List.of(
            "Allow", "Allow While Using App", "OK", "Continue"
    );

    private PermissionHandler() {}

    /**
     * Attempts to accept all pending permission dialogs.
     * Loops up to {@code maxAttempts} times so chained dialogs are handled.
     */
    public static void acceptAll(int maxAttempts) {
        boolean isAndroid = ConfigLoader.load().isAndroid();
        for (int i = 0; i < maxAttempts; i++) {
            boolean handled = isAndroid ? tryAndroidPermission() : tryIosPermission();
            if (!handled) break;
            LOG.info("Permission dialog accepted (attempt {})", i + 1);
        }
    }

    // -------------------------------------------------------------------------
    // Android
    // -------------------------------------------------------------------------

    private static boolean tryAndroidPermission() {
        AppiumDriver driver = DriverManager.getDriver();
        for (String text : ANDROID_ALLOW_TEXTS) {
            List<WebElement> buttons = driver.findElements(
                    By.xpath("//android.widget.Button[@text='" + text + "']"));
            if (!buttons.isEmpty()) {
                LOG.info("Tapping Android permission button: '{}'", text);
                buttons.get(0).click();
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // iOS
    // -------------------------------------------------------------------------

    private static boolean tryIosPermission() {
        IOSDriver iosDriver = (IOSDriver) DriverManager.getDriver();
        for (String text : IOS_ALLOW_TEXTS) {
            List<WebElement> buttons = iosDriver.findElements(
                    By.xpath("//XCUIElementTypeButton[@name='" + text + "']"));
            if (!buttons.isEmpty()) {
                LOG.info("Tapping iOS permission button: '{}'", text);
                buttons.get(0).click();
                return true;
            }
        }

        // Fallback: accept using the built-in Appium alert handler
        try {
            iosDriver.switchTo().alert().accept();
            LOG.info("Accepted iOS alert via switchTo().alert()");
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
