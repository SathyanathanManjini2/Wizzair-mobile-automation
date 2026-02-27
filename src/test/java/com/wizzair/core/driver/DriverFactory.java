package com.wizzair.core.driver;

import com.wizzair.config.DeviceConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Factory responsible for creating and returning the correct {@link AppiumDriver}
 * instance based on the active platform in {@link DeviceConfig}.
 *
 * <p>Uses the strongly-typed Options classes (UiAutomator2Options / XCUITestOptions)
 * instead of raw DesiredCapabilities, which is the modern, recommended approach.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {}

    /**
     * Creates and returns a platform-specific Appium driver.
     *
     * @param cfg resolved device configuration
     * @return ready-to-use {@link AppiumDriver}
     */
    public static AppiumDriver createDriver(DeviceConfig cfg) {
        try {
            URL serverUrl = new URL(cfg.getAppiumServerUrl());
            return cfg.isAndroid() ? buildAndroidDriver(cfg, serverUrl)
                                   : buildIosDriver(cfg, serverUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + cfg.getAppiumServerUrl(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Android
    // -------------------------------------------------------------------------

    private static AndroidDriver buildAndroidDriver(DeviceConfig cfg, URL serverUrl) {
        LOG.info("Creating AndroidDriver → device={}, platformVersion={}",
                cfg.getDeviceName(), cfg.getPlatformVersion());

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformVersion(cfg.getPlatformVersion())
                .setDeviceName(cfg.getDeviceName())
                .setAutomationName(cfg.getAutomationName())
                .setNewCommandTimeout(Duration.ofSeconds(cfg.getNewCommandTimeout()))
                .setNoReset(cfg.isNoReset())
                .setFullReset(cfg.isFullReset())
                .autoGrantPermissions();   // start with auto-grant; tests that need manual
                                           // permission handling can toggle this via capability

        // UDID is mandatory for real devices; skip for emulators
        if (cfg.getUdid() != null && !cfg.getUdid().isBlank()) {
            options.setUdid(cfg.getUdid());
        }

        // If an .apk path is provided install it; otherwise use the already-installed app
        if (cfg.getAppPath() != null && !cfg.getAppPath().isBlank()) {
            options.setApp(cfg.getAppPath());
        } else {
            options.setAppPackage(cfg.getAppPackage())
                   .setAppActivity(cfg.getAppActivity());
        }

        return new AndroidDriver(serverUrl, options);
    }

    // -------------------------------------------------------------------------
    // iOS
    // -------------------------------------------------------------------------

    private static IOSDriver buildIosDriver(DeviceConfig cfg, URL serverUrl) {
        LOG.info("Creating IOSDriver → device={}, platformVersion={}",
                cfg.getDeviceName(), cfg.getPlatformVersion());

        XCUITestOptions options = new XCUITestOptions()
                .setPlatformVersion(cfg.getPlatformVersion())
                .setDeviceName(cfg.getDeviceName())
                .setAutomationName(cfg.getAutomationName())
                .setNewCommandTimeout(Duration.ofSeconds(cfg.getNewCommandTimeout()))
                .setNoReset(cfg.isNoReset())
                .setFullReset(cfg.isFullReset())
                .setWdaLaunchTimeout(Duration.ofMillis(cfg.getWdaLaunchTimeout()))
                .setWdaConnectionTimeout(Duration.ofMillis(cfg.getWdaConnectionTimeout()));

        if (cfg.getUdid() != null && !cfg.getUdid().isBlank()) {
            options.setUdid(cfg.getUdid());
        }

        if (cfg.getAppPath() != null && !cfg.getAppPath().isBlank()) {
            options.setApp(cfg.getAppPath());
        } else {
            options.setBundleId(cfg.getBundleId());
        }

        return new IOSDriver(serverUrl, options);
    }
}
