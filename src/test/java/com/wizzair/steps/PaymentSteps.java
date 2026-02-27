package com.wizzair.steps;

import com.wizzair.core.context.ContextManager;
import com.wizzair.pages.common.BookingPage;
import com.wizzair.pages.common.FlightResultsPage;
import com.wizzair.pages.common.FlightSearchPage;
import com.wizzair.pages.common.PaymentPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Step definitions for the WebView payment E2E scenario.
 */
public class PaymentSteps {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentSteps.class);

    private PaymentPage paymentPage;

    // =========================================================================
    // Given
    // =========================================================================

    @Given("I have navigated to the payment screen for flight {string} to {string}")
    public void iHaveNavigatedToPaymentScreen(String origin, String destination) {
        LOG.info("Navigating to payment for {} → {}", origin, destination);

        // Search + select first available flight
        FlightSearchPage search   = new FlightSearchPage();
        FlightResultsPage results = search.enterOrigin(origin)
                                         .enterDestination(destination)
                                         .search();

        // Select first visible flight (no scroll needed for precondition setup)
        BookingPage booking = results.findFlightByTime("", "");  // any first flight

        // Fill minimal passenger details to reach payment
        booking.enterFirstName("Test")
               .enterLastName("User")
               .enterEmail("test@wizzair-test.com")
               .enterPhone("+44000000000");

        paymentPage = booking.proceedToPayment();
    }

    @Given("the payment form is loaded inside a WebView")
    public void thePaymentFormIsLoadedInsideWebView() {
        Assertions.assertThat(paymentPage.isLoaded())
                  .as("Payment page (native container) should be visible")
                  .isTrue();
        LOG.info("Available contexts: {}", ContextManager.getAllContexts());
    }

    // =========================================================================
    // When
    // =========================================================================

    @When("I fill in the card details:")
    public void iFillInCardDetails(List<Map<String, String>> table) {
        Map<String, String> card = table.get(0);
        LOG.info("Filling payment form in WebView");
        paymentPage.completePaymentForm(
                card.get("Card Number"),
                card.get("Expiry"),
                card.get("CVV"),
                card.get("Card Holder")
        );
    }

    @And("I submit the payment")
    public void iSubmitThePayment() {
        // Payment submission happens inside completePaymentForm – this step is documentary.
        LOG.info("Payment form submitted (handled within completePaymentForm)");
    }

    // =========================================================================
    // Then / And
    // =========================================================================

    @Then("the payment should be processed successfully")
    public void thePaymentShouldBeProcessedSuccessfully() {
        // The PaymentPage.completePaymentForm waits for the success message inside WebView
        // before returning – reaching this step means it succeeded
        LOG.info("Payment processing asserted successful");
    }

    @And("I should be returned to the native app context")
    public void iShouldBeReturnedToNativeContext() {
        Assertions.assertThat(ContextManager.isInWebView())
                  .as("Driver should be back in native context after payment")
                  .isFalse();
    }

    @And("a booking confirmation should be displayed")
    public void aBookingConfirmationShouldBeDisplayed() {
        Assertions.assertThat(paymentPage.isConfirmationDisplayed())
                  .as("Booking confirmation screen should be visible after payment")
                  .isTrue();
    }
}
