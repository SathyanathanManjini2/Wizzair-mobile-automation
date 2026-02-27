package com.wizzair.core.driver;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local holder for the {@link AppiumDriver} instance.
 *
 * <p>Using {@link ThreadLocal} ensures that parallel test execution is safe –
 * each test thread owns its own driver reference without interference.
 *
 * <p>Lifecycle managed by {@link com.wizzair.hooks.DriverHooks}.
 */
public final class DriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(DriverManager.class);

    /** Thread-local storage – one driver per thread. */
    private static final ThreadLocal<AppiumDriver> DRIVER_HOLDER = new ThreadLocal<>();

    private DriverManager() {}

    /**
     * Stores a driver for the current thread.
     *
     * @param driver initialised Appium driver
     */
    public static void setDriver(AppiumDriver driver) {
        LOG.debug("Registering driver for thread {}", Thread.currentThread().getId());
        DRIVER_HOLDER.set(driver);
    }

    /**
     * Returns the driver bound to the current thread.
     *
     * @throws IllegalStateException if no driver has been set
     */
    public static AppiumDriver getDriver() {
        AppiumDriver driver = DRIVER_HOLDER.get();
        if (driver == null) {
            throw new IllegalStateException(
                "No AppiumDriver found for thread " + Thread.currentThread().getId() +
                ". Ensure DriverHooks has run before accessing the driver.");
        }
        return driver;
    }

    /**
     * Quits and removes the driver for the current thread.
     * Safe to call even if no driver is present.
     */
    public static void quitDriver() {
        AppiumDriver driver = DRIVER_HOLDER.get();
        if (driver != null) {
            try {
                LOG.info("Quitting driver for thread {}", Thread.currentThread().getId());
                driver.quit();
            } catch (Exception e) {
                LOG.warn("Exception while quitting driver", e);
            } finally {
                DRIVER_HOLDER.remove();
            }
        }
    }
}
