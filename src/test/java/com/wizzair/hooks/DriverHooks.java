package com.wizzair.hooks;

import com.wizzair.config.ConfigLoader;
import com.wizzair.config.DeviceConfig;
import com.wizzair.core.driver.DriverFactory;
import com.wizzair.core.driver.DriverManager;
import com.wizzair.pages.common.PermissionHandler;
import com.wizzair.utils.ScreenshotHelper;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber lifecycle hooks for driver initialisation and teardown.
 *
 * <p>Order:
 * <ol>
 *   <li>{@link #setUp(Scenario)} – create driver, handle first-launch permissions</li>
 *   <li>Scenario steps execute</li>
 *   <li>{@link #attachScreenshotOnFailure(Scenario)} – screenshot on failure</li>
 *   <li>{@link #tearDown(Scenario)} – quit driver</li>
 * </ol>
 */
public class DriverHooks {

    private static final Logger LOG = LoggerFactory.getLogger(DriverHooks.class);

    @Before(order = 0)
    public void setUp(Scenario scenario) {
        LOG.info("▶ Starting scenario: {}", scenario.getName());
        DeviceConfig cfg = ConfigLoader.load();

        // Create and register the driver
        DriverManager.setDriver(DriverFactory.createDriver(cfg));

        // Handle any first-launch permission dialogs that appear at startup
        // (only needed when autoGrantPermissions = false in config)
        if (!cfg.isAutoGrantPermissions()) {
            LOG.info("Handling initial permission dialogs");
            PermissionHandler.acceptAll(5);
        }
    }

    @AfterStep
    public void afterEachStep(Scenario scenario) {
        // Capture a screenshot after every step when the scenario has failed
        // to provide a visual trail through the failure
        if (scenario.isFailed()) {
            ScreenshotHelper.attachToReport("Failure - " + scenario.getName());
        }
    }

    @After(order = 0)
    public void tearDown(Scenario scenario) {
        LOG.info("◀ Finishing scenario: {} | Status: {}", scenario.getName(), scenario.getStatus());
        if (scenario.isFailed()) {
            ScreenshotHelper.attachToReport("Final failure screenshot");
        }
        DriverManager.quitDriver();
    }
}
