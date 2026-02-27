package com.wizzair.steps;

import com.wizzair.pages.common.FlightDetailsPage;
import com.wizzair.pages.common.PermissionHandler;
import com.wizzair.utils.DeepLinkHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step definitions for the Deep Link + Permissions feature.
 *
 * <p>Dependencies are injected via PicoContainer (Cucumber DI) through constructor.
 * The {@link FlightDetailsPage} is created lazily (after deep link opens the screen).
 */
public class DeepLinkSteps {

    private static final Logger LOG = LoggerFactory.getLogger(DeepLinkSteps.class);

    private FlightDetailsPage flightDetailsPage;

    // =========================================================================
    // Given
    // =========================================================================

    @Given("the WizzAir app is launched fresh \\(no cached state\\)")
    public void theAppIsLaunchedFresh() {
        // Driver is already created and app launched by DriverHooks.setUp()
        // This step is documentary – verifying the precondition
        LOG.info("App launched fresh – ready for deep link test");
    }

    // =========================================================================
    // When
    // =========================================================================

    @Given("I open the app using the deep link for flight {string} to {string} on {string}")
    public void iOpenTheDeepLink(String origin, String destination, String date) {
        LOG.info("Opening deep link: {} → {} on {}", origin, destination, date);
        DeepLinkHelper.openFlight(origin, destination, date);

        // Instantiate the page *after* navigation so PageFactory can find elements
        flightDetailsPage = new FlightDetailsPage();
    }

    @When("all system permission dialogs are handled")
    public void allPermissionDialogsHandled() {
        LOG.info("Handling any permission dialogs post deep link");
        // Deep links can trigger location / notification permissions
        PermissionHandler.acceptAll(5);
    }

    // =========================================================================
    // Then / And
    // =========================================================================

    @Then("the flight details screen should be displayed")
    public void theFlightDetailsScreenShouldBeDisplayed() {
        Assertions.assertThat(flightDetailsPage.isLoaded())
                  .as("Flight details screen should be visible")
                  .isTrue();
    }

    @And("the route shown should be {string}")
    public void theRouteShownShouldBe(String expectedRoute) {
        String actualRoute = flightDetailsPage.getRoute();
        Assertions.assertThat(actualRoute)
                  .as("Route label")
                  .contains(expectedRoute);
    }

    @And("the date shown should be {string}")
    public void theDateShownShouldBe(String expectedDate) {
        String actualDate = flightDetailsPage.getDate();
        Assertions.assertThat(actualDate)
                  .as("Date label")
                  .contains(expectedDate);
    }
}
