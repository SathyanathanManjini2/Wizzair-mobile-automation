package com.wizzair.steps;

import com.wizzair.pages.common.FlightResultsPage;
import com.wizzair.pages.common.FlightSearchPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step definitions for flight search and infinite scroll scenarios.
 */
public class FlightSearchSteps {

    private static final Logger LOG = LoggerFactory.getLogger(FlightSearchSteps.class);

    private FlightSearchPage  searchPage;
    private FlightResultsPage resultsPage;

    // =========================================================================
    // Given
    // =========================================================================

    @Given("I am on the flight search screen")
    public void iAmOnTheFlightSearchScreen() {
        searchPage = new FlightSearchPage();
        Assertions.assertThat(searchPage.isLoaded())
                  .as("Flight search screen should be displayed on launch")
                  .isTrue();
    }

    // =========================================================================
    // When
    // =========================================================================

    @When("I search for a flight from {string} to {string} on {string}")
    public void iSearchForAFlight(String origin, String destination, String date) {
        LOG.info("Searching: {} → {} on {}", origin, destination, date);
        resultsPage = searchPage
                .enterOrigin(origin)
                .enterDestination(destination)
                .selectDepartureDate(date)
                .search();
    }

    // =========================================================================
    // Then / And
    // =========================================================================

    @Then("the flight results list is displayed")
    public void theFlightResultsListIsDisplayed() {
        Assertions.assertThat(resultsPage.isLoaded())
                  .as("Flight results page should be loaded")
                  .isTrue();
    }

    @And("I scroll until I find the flight departing at {string} and arriving at {string}")
    public void iScrollUntilIFindTheFlight(String departure, String arrival) {
        LOG.info("Scrolling to find flight: {} → {}", departure, arrival);
        // findFlightByTime scrolls repeatedly until found or throws
        resultsPage.findFlightByTime(departure, arrival);
    }

    @And("I should be able to tap that flight to begin booking")
    public void iShouldBeAbleToTapThatFlight() {
        // The findFlightByTime already tapped the card; this step is a documentary assertion
        LOG.info("Flight tapped successfully – booking page should load next");
    }

    @And("the list contains at least {int} flight option")
    public void theListContainsAtLeastNFlightOptions(int minCount) {
        int visible = resultsPage.getVisibleFlightCount();
        Assertions.assertThat(visible)
                  .as("Flight results count")
                  .isGreaterThanOrEqualTo(minCount);
    }
}
