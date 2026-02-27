package com.wizzair.core.wait;

import com.wizzair.core.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Centralised waiting utilities for the framework.
 *
 * <p>Philosophy:
 * <ul>
 *   <li>No {@code Thread.sleep()} calls anywhere in the framework.</li>
 *   <li>Use {@link WebDriverWait} / {@link FluentWait} for element conditions.</li>
 *   <li>Use {@link Awaitility} for custom boolean/callable conditions that are not
 *       element-related (e.g. context switch, modal appearance, price change detection).</li>
 * </ul>
 */
public final class WaitStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(WaitStrategy.class);

    // Default timeouts
    public static final Duration DEFAULT_TIMEOUT  = Duration.ofSeconds(30);
    public static final Duration SHORT_TIMEOUT    = Duration.ofSeconds(10);
    public static final Duration LONG_TIMEOUT     = Duration.ofSeconds(60);
    public static final Duration POLL_INTERVAL    = Duration.ofMillis(500);

    private WaitStrategy() {}

    // =========================================================================
    // Element-based waits (delegating to Selenium WebDriverWait)
    // =========================================================================

    /** Waits until an element is visible on screen. */
    public static WebElement waitForVisible(WebElement element) {
        return waitForVisible(element, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForVisible(WebElement element, Duration timeout) {
        return buildFluentWait(timeout)
                .until(ExpectedConditions.visibilityOf(element));
    }

    /** Waits until an element is clickable (visible + enabled). */
    public static WebElement waitForClickable(WebElement element) {
        return waitForClickable(element, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForClickable(WebElement element, Duration timeout) {
        return buildFluentWait(timeout)
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /** Waits until an element is no longer visible (e.g. loading spinner disappears). */
    public static boolean waitForInvisibility(WebElement element) {
        return waitForInvisibility(element, DEFAULT_TIMEOUT);
    }

    public static boolean waitForInvisibility(WebElement element, Duration timeout) {
        try {
            return buildFluentWait(timeout)
                    .until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            return false;
        }
    }

    // =========================================================================
    // Callable / boolean conditions (Awaitility)
    // =========================================================================

    /**
     * Polls a {@link Callable<Boolean>} condition until it returns {@code true}
     * or the timeout expires.
     *
     * <pre>
     *   WaitStrategy.waitUntil(() -> driver.getContext().contains("WEBVIEW"), 30, "WebView loaded");
     * </pre>
     */
    public static void waitUntil(Callable<Boolean> condition, int timeoutSeconds, String description) {
        LOG.debug("Waiting up to {}s for: {}", timeoutSeconds, description);
        try {
            Awaitility.await()
                      .alias(description)
                      .atMost(Duration.ofSeconds(timeoutSeconds))
                      .pollInterval(POLL_INTERVAL)
                      .until(condition);
        } catch (ConditionTimeoutException e) {
            throw new RuntimeException("Timed out waiting for: " + description, e);
        }
    }

    /**
     * Waits for a supplied element reference to become non-null and visible.
     * Useful when a Supplier provides a lazily-found element.
     */
    public static WebElement waitForElement(Supplier<WebElement> elementSupplier, Duration timeout) {
        FluentWait<AppiumDriver> wait = new FluentWait<>(DriverManager.getDriver())
                .withTimeout(timeout)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        return wait.until(driver -> {
            WebElement el = elementSupplier.get();
            return (el != null && el.isDisplayed()) ? el : null;
        });
    }

    // =========================================================================
    // Convenience: check presence without throwing
    // =========================================================================

    /**
     * Returns {@code true} if the element is visible within the short timeout.
     * Does NOT throw – use for conditional logic (e.g. "is the modal present?").
     */
    public static boolean isVisible(WebElement element) {
        return isVisible(element, SHORT_TIMEOUT);
    }

    public static boolean isVisible(WebElement element, Duration timeout) {
        try {
            waitForVisible(element, timeout);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    private static WebDriverWait buildFluentWait(Duration timeout) {
        return (WebDriverWait) new WebDriverWait(DriverManager.getDriver(), timeout)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }
}
