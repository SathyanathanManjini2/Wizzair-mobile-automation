package com.wizzair.utils;

import com.wizzair.core.driver.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Utility for capturing and attaching screenshots to Allure reports.
 */
public final class ScreenshotHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotHelper.class);

    private ScreenshotHelper() {}

    /**
     * Captures a screenshot and attaches it to the current Allure test report.
     *
     * @param name label shown in Allure (e.g. "After price change modal")
     */
    public static void attachToReport(String name) {
        try {
            byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), "png");
            LOG.debug("Screenshot attached: {}", name);
        } catch (Exception e) {
            LOG.warn("Failed to capture screenshot '{}': {}", name, e.getMessage());
        }
    }
}
