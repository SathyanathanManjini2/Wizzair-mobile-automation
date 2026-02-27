package com.wizzair.steps;

import com.wizzair.pages.common.BookingPage;
import com.wizzair.pages.common.FlightResultsPage;
import com.wizzair.pages.common.FlightSearchPage;
import com.wizzair.utils.ScreenshotHelper;
import io.cucumber.java.DataTableType;
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
 * Step definitions for the price-change modal scenario.
 *
 * <p>The price change modal is non-deterministic in real testing – it appears
 * when the backend price updates between selection and booking. In a CI environment
 * this would be triggered via a mock or test flag on the backend.
 */
public class PriceChangeSteps {

    private static final Logger LOG = LoggerFactory.getLogger(PriceChangeSteps.class);

    private BookingPage bookingPage;
    private String      capturedNewPrice;

    // =========================================================================
    // Given
    // =========================================================================

    @Given("I have selected the flight from {string} to {string} departing at {string}")
    public void iHaveSelectedTheFlight(String origin, String destination, String time) {
        LOG.info("Selecting flight {} → {} at {}", origin, destination, time);
        FlightSearchPage search  = new FlightSearchPage();
        FlightResultsPage results = search.enterOrigin(origin)
                                         .enterDestination(destination)
                                         .search();          // date omitted for brevity; extend as needed
        results.findFlightByTime(time, "");   // arrival wildcard – extend FlightResultsPage as needed
    }

    @Given("I am on the passenger details screen")
    public void iAmOnThePassengerDetailsScreen() {
        bookingPage = new BookingPage();
        Assertions.assertThat(bookingPage.isLoaded())
                  .as("Booking / passenger details screen should be displayed")
                  .isTrue();
    }

    // =========================================================================
    // When
    // =========================================================================

    @When("I enter my passenger details:")
    public void iEnterMyPassengerDetails(List<Map<String, String>> table) {
        Map<String, String> row = table.get(0);   // single-passenger scenario
        bookingPage.enterFirstName(row.get("First Name"))
                   .enterLastName(row.get("Last Name"))
                   .enterEmail(row.get("Email"))
                   .enterPhone(row.get("Phone"));
        LOG.info("Passenger details entered for: {} {}", row.get("First Name"), row.get("Last Name"));
    }

    @And("a price change modal appears during the booking flow")
    public void aPriceChangeModalAppears() {
        // In a real test, this would be triggered by the backend (via a test flag or mock).
        // Here we assert the modal IS visible (test environment must be configured to trigger it).
        ScreenshotHelper.attachToReport("Before price change check");
        Assertions.assertThat(bookingPage.isPriceChangeModalVisible())
                  .as("Price change modal should be visible")
                  .isTrue();
    }

    @And("no price change modal appears")
    public void noPriceChangeModalAppears() {
        Assertions.assertThat(bookingPage.isPriceChangeModalVisible())
                  .as("Price change modal should NOT be visible")
                  .isFalse();
    }

    // =========================================================================
    // Then / And
    // =========================================================================

    @Then("I should see the new price displayed in the modal")
    public void iShouldSeeTheNewPriceInModal() {
        capturedNewPrice = bookingPage.getNewPrice();
        LOG.info("New price displayed: {}", capturedNewPrice);
        Assertions.assertThat(capturedNewPrice)
                  .as("New price should not be empty")
                  .isNotBlank();
    }

    @When("I accept the new price")
    public void iAcceptTheNewPrice() {
        bookingPage.acceptPriceChange();
        LOG.info("Price change accepted – modal dismissed");
    }

    @Then("the modal should close")
    public void theModalShouldClose() {
        Assertions.assertThat(bookingPage.isPriceChangeModalVisible())
                  .as("Price change modal should be dismissed")
                  .isFalse();
    }

    @And("I should be able to continue the booking process")
    public void iShouldBeAbleToContinueBooking() {
        // Verify the booking page is still in a usable state
        Assertions.assertThat(bookingPage.isLoaded())
                  .as("Booking page should still be displayed after modal dismissal")
                  .isTrue();
    }

    @Then("I should proceed directly to the payment screen")
    public void iShouldProceedToPaymentScreen() {
        var paymentPage = bookingPage.proceedToPayment();
        Assertions.assertThat(paymentPage.isLoaded())
                  .as("Payment screen should be displayed")
                  .isTrue();
    }
}
