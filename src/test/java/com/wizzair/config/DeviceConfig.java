package com.wizzair.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Maps device/environment YAML configuration to a Java object.
 * Jackson deserialises android-config.yaml or ios-config.yaml into this class.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfig {

    private String platform;
    private String platformVersion;
    private String deviceName;
    private String udid;

    // Android-only
    private String appPackage;
    private String appActivity;

    // iOS-only
    private String bundleId;

    // Shared
    private String appPath;
    private String appiumServerUrl;
    private String automationName;
    private int    newCommandTimeout = 300;
    private boolean autoGrantPermissions = false;
    private boolean autoAcceptAlerts = false;
    private boolean noReset = false;
    private boolean fullReset = false;
    private String deepLinkScheme = "wizzair";

    // iOS extras
    private int wdaLaunchTimeout    = 120000;
    private int wdaConnectionTimeout = 120000;

    /** Convenience – returns true when running on Android. */
    public boolean isAndroid() {
        return "android".equalsIgnoreCase(platform);
    }

    /** Convenience – returns true when running on iOS. */
    public boolean isIos() {
        return "ios".equalsIgnoreCase(platform);
    }
}
