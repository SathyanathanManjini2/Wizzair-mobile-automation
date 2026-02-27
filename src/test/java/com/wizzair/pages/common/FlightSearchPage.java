package com.wizzair.pages.common;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the WizzAir flight search / home screen.
 *
 * <p>Locator strategy:
 * <ul>
 *   <li>Prefer accessibility IDs / content descriptions (stable across versions).</li>
 *   <li>Fall back to resource-id for Android, name/label for iOS.</li>
 *   <li>Avoid XPath based on visual position or text that may be localised.</li>
 * </ul>
 */
public class FlightSearchPage extends BasePage {

    // =========================================================================
    // Locators – cross-platform via separate Android / iOS annotations
    // =========================================================================

    @AndroidFindBy(accessibility = "Origin airport")
    @iOSXCUITFindBy(accessibility = "Origin airport")
    private WebElement originField;

    @AndroidFindBy(accessibility = "Destination airport")
    @iOSXCUITFindBy(accessibility = "Destination airport")
    private WebElement destinationField;

    @AndroidFindBy(accessibility = "Departure date")
    @iOSXCUITFindBy(accessibility = "Departure date")
    private WebElement departureDateField;

    @AndroidFindBy(accessibility = "Return date")
    @iOSXCUITFindBy(accessibility = "Return date")
    private WebElement returnDateField;

    @AndroidFindBy(accessibility = "Search flights")
    @iOSXCUITFindBy(accessibility = "Search flights")
    private WebElement searchButton;

    @AndroidFindBy(accessibility = "Passengers")
    @iOSXCUITFindBy(accessibility = "Passengers")
    private WebElement passengersButton;

    // =========================================================================
    // Actions
    // =========================================================================

    /** Enters the origin city or IATA code in the origin field. */
    public FlightSearchPage enterOrigin(String origin) {
        LOG.info("Entering origin: {}", origin);
        type(originField, origin);
        return this;
    }

    /** Enters the destination city or IATA code. */
    public FlightSearchPage enterDestination(String destination) {
        LOG.info("Entering destination: {}", destination);
        type(destinationField, destination);
        return this;
    }

    /** Selects a departure date from the calendar. */
    public FlightSearchPage selectDepartureDate(String date) {
        LOG.info("Selecting departure date: {}", date);
        tap(departureDateField);
        // The date picker is a separate component – delegate to DatePickerPage or
        // use accessibility label to find the date cell directly.
        driver().findElement(
            io.appium.java_client.AppiumBy.accessibilityId(date)
        ).click();
        return this;
    }

    /** Taps the Search Flights button and returns the results page. */
    public FlightResultsPage search() {
        LOG.info("Tapping Search button");
        tap(searchButton);
        return new FlightResultsPage();
    }

    // =========================================================================
    // Assertions / state
    // =========================================================================

    @Override
    public boolean isLoaded() {
        return isDisplayed(searchButton);
    }
}
