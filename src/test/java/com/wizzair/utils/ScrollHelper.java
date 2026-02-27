package com.wizzair.utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Utility class for performing scroll gestures in a platform-agnostic way.
 *
 * <p>Uses W3C Actions API (supported by both UiAutomator2 and XCUITest)
 * rather than the deprecated TouchActions.
 *
 * <p>For Android, UiAutomator2 scrollable strategy is preferred when available
 * (faster, more reliable); falls back to W3C swipe for iOS and non-scrollable containers.
 */
public final class ScrollHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ScrollHelper.class);

    // Fraction of screen height used for the swipe gesture
    private static final double SCROLL_START_RATIO = 0.75;
    private static final double SCROLL_END_RATIO   = 0.25;

    private ScrollHelper() {}

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Scrolls down by swiping from ~75% to ~25% of the screen height.
     *
     * @param driver active Appium driver
     * @return {@code true} if the swipe was performed, {@code false} if the screen
     *         dimensions could not be obtained
     */
    public static boolean scrollDown(AppiumDriver driver) {
        return swipe(driver, Direction.DOWN);
    }

    /**
     * Scrolls up by swiping from ~25% to ~75% of the screen height.
     */
    public static boolean scrollUp(AppiumDriver driver) {
        return swipe(driver, Direction.UP);
    }

    /**
     * Android-specific: uses UiAutomator2 scroll on a {@code scrollable} view to
     * locate an element by text. Faster than repeated W3C swipes.
     *
     * @param driver    active Android driver
     * @param text      visible text of the element to scroll to
     */
    public static void scrollToTextAndroid(AndroidDriver driver, String text) {
        LOG.debug("UiAutomator2 scroll to text: '{}'", text);
        String uiSelector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView("
                          + "new UiSelector().text(\"" + text + "\"))";
        driver.findElement(io.appium.java_client.AppiumBy.androidUIAutomator(uiSelector));
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private enum Direction { UP, DOWN }

    private static boolean swipe(AppiumDriver driver, Direction direction) {
        try {
            Dimension size = driver.manage().window().getSize();
            int midX = size.getWidth() / 2;
            int startY, endY;

            if (direction == Direction.DOWN) {
                startY = (int) (size.getHeight() * SCROLL_START_RATIO);
                endY   = (int) (size.getHeight() * SCROLL_END_RATIO);
            } else {
                startY = (int) (size.getHeight() * SCROLL_END_RATIO);
                endY   = (int) (size.getHeight() * SCROLL_START_RATIO);
            }

            performSwipe(driver, midX, startY, midX, endY);
            return true;
        } catch (Exception e) {
            LOG.warn("Scroll gesture failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Executes a W3C touch swipe action from (startX, startY) to (endX, endY).
     */
    private static void performSwipe(AppiumDriver driver,
                                     int startX, int startY,
                                     int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0)
                .addAction(finger.createPointerMove(
                    Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(
                    Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(List.of(swipe));
        LOG.debug("Swipe performed: ({},{}) → ({},{})", startX, startY, endX, endY);
    }
}
