package com.wizzair.core.context;

import com.wizzair.core.driver.DriverManager;
import com.wizzair.core.wait.WaitStrategy;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Manages switching between native app context and WebView context.
 *
 * <p>Appium exposes multiple contexts when a hybrid app renders a WebView:
 * <ul>
 *   <li>{@code NATIVE_APP} – the native layer</li>
 *   <li>{@code WEBVIEW_<package>} – the embedded browser (Android)</li>
 *   <li>{@code WEBVIEW_<pid>} – the embedded browser (iOS)</li>
 * </ul>
 *
 * <p>This class waits for the WebView context to appear before switching,
 * avoiding race conditions that cause fragile tests.
 */
public final class ContextManager {

    private static final Logger LOG = LoggerFactory.getLogger(ContextManager.class);
    private static final String NATIVE_CONTEXT  = "NATIVE_APP";
    private static final String WEBVIEW_PREFIX  = "WEBVIEW";

    private ContextManager() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Switches the driver to the first available WebView context.
     * Waits up to {@code timeoutSeconds} for a WebView to appear.
     */
    public static void switchToWebView(int timeoutSeconds) {
        AppiumDriver driver = DriverManager.getDriver();

        LOG.info("Waiting for WebView context (timeout={}s)", timeoutSeconds);
        WaitStrategy.waitUntil(
            () -> hasWebViewContext(driver),
            timeoutSeconds,
            "WebView context to appear"
        );

        Set<String> contexts = driver.getContextHandles();
        String webViewContext = contexts.stream()
                .filter(ctx -> ctx.startsWith(WEBVIEW_PREFIX))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                    "No WebView context found among: " + contexts));

        LOG.info("Switching to context: {}", webViewContext);
        driver.context(webViewContext);
    }

    /**
     * Switches back to the native app context.
     */
    public static void switchToNativeApp() {
        LOG.info("Switching to NATIVE_APP context");
        DriverManager.getDriver().context(NATIVE_CONTEXT);
    }

    /**
     * Returns the current active context name.
     */
    public static String getCurrentContext() {
        return DriverManager.getDriver().getContext();
    }

    /**
     * Returns {@code true} if the driver is currently in a WebView context.
     */
    public static boolean isInWebView() {
        return getCurrentContext().startsWith(WEBVIEW_PREFIX);
    }

    /**
     * Returns all available contexts (for debugging).
     */
    public static Set<String> getAllContexts() {
        return DriverManager.getDriver().getContextHandles();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static boolean hasWebViewContext(AppiumDriver driver) {
        try {
            Set<String> contexts = driver.getContextHandles();
            boolean found = contexts.stream().anyMatch(c -> c.startsWith(WEBVIEW_PREFIX));
            if (!found) {
                LOG.debug("Contexts available: {} – no WebView yet", contexts);
            }
            return found;
        } catch (Exception e) {
            LOG.warn("Exception while checking contexts: {}", e.getMessage());
            return false;
        }
    }
}
