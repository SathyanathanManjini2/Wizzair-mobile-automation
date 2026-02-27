package com.wizzair.pages.common;

import com.wizzair.core.wait.WaitStrategy;
import com.wizzair.utils.ScrollHelper;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the flight search results screen.
 *
 * <p>Key challenge: results load dynamically as the user scrolls (infinite scroll).
 * {@link #findFlightByTime(String, String)} keeps scrolling until the target
 * flight row appears or the list is exhausted.
 */
public class FlightResultsPage extends BasePage {

    // =========================================================================
    // Locators
    // =========================================================================

    /** Container that holds all visible flight cards. */
    @AndroidFindBy(accessibility = "Flight results list")
    @iOSXCUITFindBy(accessibility = "Flight results list")
    private WebElement resultsContainer;

    /** Individual flight card (each card is one result row). */
    @AndroidFindBy(accessibility = "Flight card")
    @iOSXCUITFindBy(accessibility = "Flight card")
    private List<WebElement> flightCards;

    /** Loading spinner shown when the next batch of results is being fetched. */
    @AndroidFindBy(accessibility = "Loading flights")
    @iOSXCUITFindBy(accessibility = "Loading flights")
    private WebElement loadingSpinner;

    // =========================================================================
    // Actions
    // =========================================================================

    /**
     * Scrolls through the results list until a flight matching the given
     * departure and arrival time is found, then taps it.
     *
     * @param departureTime  e.g. "06:00"
     * @param arrivalTime    e.g. "08:30"
     * @return {@link BookingPage} after tapping the flight card
     * @throws RuntimeException if the flight is not found after exhausting scroll
     */
    public BookingPage findFlightByTime(String departureTime, String arrivalTime) {
        LOG.info("Searching for flight: {} → {}", departureTime, arrivalTime);

        final int maxScrollAttempts = 20;
        for (int attempt = 0; attempt < maxScrollAttempts; attempt++) {

            // Wait for any loading indicator to disappear first
            WaitStrategy.waitForInvisibility(loadingSpinner, WaitStrategy.SHORT_TIMEOUT);

            // Look for the target flight in currently visible cards
            WebElement card = findCardByTimes(departureTime, arrivalTime);
            if (card != null) {
                LOG.info("Flight found – tapping card");
                tap(card);
                return new BookingPage();
            }

            // Not yet visible – scroll down to load more results
            LOG.debug("Flight not visible on scroll attempt {}; scrolling down", attempt + 1);
            boolean scrolled = ScrollHelper.scrollDown(driver());
            if (!scrolled) {
                break; // Reached the bottom of the list
            }
        }

        throw new RuntimeException(
            "Flight (" + departureTime + " → " + arrivalTime + ") not found after scrolling.");
    }

    /**
     * Returns the number of flight cards currently visible without scrolling.
     */
    public int getVisibleFlightCount() {
        return flightCards.size();
    }

    // =========================================================================
    // Assertions / state
    // =========================================================================

    @Override
    public boolean isLoaded() {
        return isDisplayed(resultsContainer);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Searches visible flight cards for one matching the given times.
     * Uses accessibility label / content description to avoid fragile positional XPath.
     */
    private WebElement findCardByTimes(String departureTime, String arrivalTime) {
        String targetLabel = departureTime + " " + arrivalTime;  // label format in the app

        // Try accessibility ID first (most stable)
        List<WebElement> matches = driver().findElements(
            AppiumBy.accessibilityId(targetLabel));
        if (!matches.isEmpty()) return matches.get(0);

        // Fallback: search inside each card for the time labels
        for (WebElement card : flightCards) {
            try {
                String cardText = card.getAttribute("content-desc");
                if (cardText != null && cardText.contains(departureTime)
                                     && cardText.contains(arrivalTime)) {
                    return card;
                }
            } catch (Exception ignored) {
                // Stale element – the list recycled; next scroll will refresh
            }
        }
        return null;
    }
}
