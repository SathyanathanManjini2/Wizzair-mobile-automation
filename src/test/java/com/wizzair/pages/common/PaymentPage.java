package com.wizzair.pages.common;

import com.wizzair.core.context.ContextManager;
import com.wizzair.core.wait.WaitStrategy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the payment step.
 *
 * <p>The payment form is rendered inside a WebView, so this page
 * demonstrates switching between native and web contexts.
 *
 * <p>Steps:
 * <ol>
 *   <li>Wait for WebView context to appear</li>
 *   <li>Switch driver to WebView</li>
 *   <li>Fill card details using standard Selenium CSS/XPath selectors</li>
 *   <li>Submit and switch back to native context</li>
 * </ol>
 */
public class PaymentPage extends BasePage {

    // =========================================================================
    // Native context locators (before switching to WebView)
    // =========================================================================

    @AndroidFindBy(accessibility = "Payment screen")
    @iOSXCUITFindBy(accessibility = "Payment screen")
    private WebElement paymentScreenIndicator;

    // =========================================================================
    // WebView selectors (standard CSS selectors used after context switch)
    // These are normal HTML element selectors visible in the embedded browser.
    // =========================================================================

    private static final String CARD_NUMBER_SELECTOR    = "input[data-cy='card-number']";
    private static final String CARD_EXPIRY_SELECTOR    = "input[data-cy='card-expiry']";
    private static final String CARD_CVV_SELECTOR       = "input[data-cy='card-cvv']";
    private static final String CARD_HOLDER_SELECTOR    = "input[data-cy='card-holder']";
    private static final String PAY_BUTTON_SELECTOR     = "button[data-cy='pay-button']";
    private static final String SUCCESS_MESSAGE_SELECTOR = "[data-cy='payment-success']";

    private static final int WEBVIEW_TIMEOUT_SECONDS = 30;

    // =========================================================================
    // Actions
    // =========================================================================

    /**
     * Switches to the WebView context, fills in the payment form,
     * submits it, and switches back to the native app.
     *
     * @param cardNumber  16-digit test card number
     * @param expiry      MM/YY format
     * @param cvv         3-digit CVV
     * @param cardHolder  cardholder name
     * @return this page (for assertion chaining)
     */
    public PaymentPage completePaymentForm(String cardNumber, String expiry,
                                           String cvv, String cardHolder) {
        LOG.info("Switching to WebView for payment form");
        ContextManager.switchToWebView(WEBVIEW_TIMEOUT_SECONDS);

        try {
            fillWebField(CARD_NUMBER_SELECTOR, cardNumber);
            fillWebField(CARD_EXPIRY_SELECTOR, expiry);
            fillWebField(CARD_CVV_SELECTOR, cvv);
            fillWebField(CARD_HOLDER_SELECTOR, cardHolder);

            LOG.info("Submitting payment form");
            driver().findElement(By.cssSelector(PAY_BUTTON_SELECTOR)).click();

            // Wait for the success message inside the WebView
            WaitStrategy.waitUntil(
                () -> !driver().findElements(By.cssSelector(SUCCESS_MESSAGE_SELECTOR)).isEmpty(),
                WEBVIEW_TIMEOUT_SECONDS,
                "Payment success message in WebView"
            );

        } finally {
            // Always return to native context regardless of outcome
            LOG.info("Switching back to NATIVE_APP context");
            ContextManager.switchToNativeApp();
        }

        return this;
    }

    /**
     * Returns {@code true} if the post-payment confirmation is shown
     * in the native layer after the WebView flow completes.
     */
    public boolean isConfirmationDisplayed() {
        // After returning to native, look for the booking confirmation element
        return !driver().findElements(
            io.appium.java_client.AppiumBy.accessibilityId("Booking confirmation")
        ).isEmpty();
    }

    // =========================================================================
    // Assertions / state
    // =========================================================================

    @Override
    public boolean isLoaded() {
        return isDisplayed(paymentScreenIndicator);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Fills a WebView input field located by CSS selector. */
    private void fillWebField(String cssSelector, String value) {
        WebElement field = WaitStrategy.waitForElement(
            () -> driver().findElement(By.cssSelector(cssSelector)),
            WaitStrategy.DEFAULT_TIMEOUT
        );
        field.clear();
        field.sendKeys(value);
    }
}
