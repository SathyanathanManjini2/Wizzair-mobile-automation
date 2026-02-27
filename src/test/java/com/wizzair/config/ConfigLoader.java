package com.wizzair.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Loads device configuration from a YAML file at test startup.
 *
 * <p>Resolution order (highest priority first):
 * <ol>
 *   <li>JVM system properties (e.g. {@code -Dplatform=android})</li>
 *   <li>Values from the YAML file ({@code android-config.yaml} or {@code ios-config.yaml})</li>
 * </ol>
 *
 * <p>Usage:
 * <pre>
 *   DeviceConfig cfg = ConfigLoader.load();
 * </pre>
 */
public final class ConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
    private static DeviceConfig instance;

    private ConfigLoader() {}

    /**
     * Returns the singleton {@link DeviceConfig}, loading it on first call.
     */
    public static synchronized DeviceConfig load() {
        if (instance == null) {
            instance = resolveConfig();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static DeviceConfig resolveConfig() {
        // 1. Determine platform from system property (default: android)
        String platform = System.getProperty("platform", "android").toLowerCase();
        String yamlFile = platform.equals("ios") ? "configs/ios-config.yaml"
                                                  : "configs/android-config.yaml";

        LOG.info("Loading device config from: {}", yamlFile);

        DeviceConfig cfg = loadYaml(yamlFile);

        // 2. Allow individual system properties to override YAML values
        overrideFromSystemProperties(cfg);

        LOG.info("Active config → platform={}, device={}, automationName={}",
                cfg.getPlatform(), cfg.getDeviceName(), cfg.getAutomationName());
        return cfg;
    }

    private static DeviceConfig loadYaml(String resourcePath) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = ConfigLoader.class.getClassLoader()
                                                .getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Config file not found on classpath: " + resourcePath);
            }
            return mapper.readValue(is, DeviceConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + resourcePath, e);
        }
    }

    private static void overrideFromSystemProperties(DeviceConfig cfg) {
        applyIfSet("deviceName",      v -> cfg.setDeviceName(v));
        applyIfSet("udid",            v -> cfg.setUdid(v));
        applyIfSet("appPath",         v -> cfg.setAppPath(v));
        applyIfSet("appiumServerUrl", v -> cfg.setAppiumServerUrl(v));
        applyIfSet("platformVersion", v -> cfg.setPlatformVersion(v));
    }

    @FunctionalInterface
    private interface StringConsumer { void accept(String value); }

    private static void applyIfSet(String key, StringConsumer setter) {
        String val = System.getProperty(key);
        if (val != null && !val.isBlank()) {
            setter.accept(val);
        }
    }
}
