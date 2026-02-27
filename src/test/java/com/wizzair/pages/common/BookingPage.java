package com.wizzair.pages.common;

import com.wizzair.core.wait.WaitStrategy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the flight booking flow.
 *
 * <p>Handles:
 * <ul>
 *   <li>Passenger details form</li>
 *   <li>Price-change modal that may appear mid-flow</li>
 *   <li>Navigating to payment</li>
 * </ul>
 */
public class BookingPage extends BasePage {

    // =========================================================================
    // Locators
    // =========================================================================

    @AndroidFindBy(accessibility = "Booking header")
    @iOSXCUITFindBy(accessibility = "Booking header")
    private WebElement bookingHeader;

    @AndroidFindBy(accessibility = "First name")
    @iOSXCUITFindBy(accessibility = "First name")
    private WebElement firstNameField;

    @AndroidFindBy(accessibility = "Last name")
    @iOSXCUITFindBy(accessibility = "Last name")
    private WebElement lastNameField;

    @AndroidFindBy(accessibility = "Email")
    @iOSXCUITFindBy(accessibility = "Email")
    private WebElement emailField;

    @AndroidFindBy(accessibility = "Phone number")
    @iOSXCUITFindBy(accessibility = "Phone number")
    private WebElement phoneField;

    @AndroidFindBy(accessibility = "Continue to payment")
    @iOSXCUITFindBy(accessibility = "Continue to payment")
    private WebElement continueToPaymentButton;

    // --- Price-change modal ---

    @AndroidFindBy(accessibility = "Price changed modal")
    @iOSXCUITFindBy(accessibility = "Price changed modal")
    private WebElement priceChangeModal;

    @AndroidFindBy(accessibility = "Accept new price")
    @iOSXCUITFindBy(accessibility = "Accept new price")
    private WebElement acceptNewPriceButton;

    @AndroidFindBy(accessibility = "New price amount")
    @iOSXCUITFindBy(accessibility = "New price amount")
    private WebElement newPriceLabel;

    // =========================================================================
    // Actions
    // =========================================================================

    public BookingPage enterFirstName(String firstName) {
        LOG.info("Entering first name: {}", firstName);
        type(firstNameField, firstName);
        return this;
    }

    public BookingPage enterLastName(String lastName) {
        LOG.info("Entering last name: {}", lastName);
        type(lastNameField, lastName);
        return this;
    }

    public BookingPage enterEmail(String email) {
        LOG.info("Entering email: {}", email);
        type(emailField, email);
        return this;
    }

    public BookingPage enterPhone(String phone) {
        LOG.info("Entering phone: {}", phone);
        type(phoneField, phone);
        return this;
    }

    /**
     * Returns the current value of the first name field (used after app resume
     * to verify data was preserved).
     */
    public String getFirstNameValue() {
        return firstNameField.getAttribute("text");
    }

    public String getLastNameValue() {
        return lastNameField.getAttribute("text");
    }

    // =========================================================================
    // Price-change modal
    // =========================================================================

    /**
     * Returns {@code true} if the price-change modal is currently visible.
     * Uses a short timeout to avoid blocking the flow unnecessarily.
     */
    public boolean isPriceChangeModalVisible() {
        return WaitStrategy.isVisible(priceChangeModal, WaitStrategy.SHORT_TIMEOUT);
    }

    /**
     * Returns the new price displayed in the modal.
     */
    public String getNewPrice() {
        return getText(newPriceLabel);
    }

    /**
     * Accepts the new price in the modal and dismisses it.
     */
    public BookingPage acceptPriceChange() {
        LOG.info("Accepting price change: {}", getNewPrice());
        tap(acceptNewPriceButton);
        return this;
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    /**
     * Proceeds to the payment screen. Before tapping, checks for a price-change
     * modal and handles it automatically if present.
     */
    public PaymentPage proceedToPayment() {
        // Handle price change modal if it appears
        if (isPriceChangeModalVisible()) {
            LOG.warn("Price change modal detected – accepting new price");
            acceptPriceChange();
        }
        LOG.info("Tapping 'Continue to payment'");
        tap(continueToPaymentButton);
        return new PaymentPage();
    }

    // =========================================================================
    // Assertions / state
    // =========================================================================

    @Override
    public boolean isLoaded() {
        return isDisplayed(bookingHeader);
    }
}
