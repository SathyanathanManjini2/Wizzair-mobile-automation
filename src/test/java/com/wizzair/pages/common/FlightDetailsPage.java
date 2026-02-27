package com.wizzair.pages.common;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the flight details screen that opens after a deep link.
 */
public class FlightDetailsPage extends BasePage {

    @AndroidFindBy(accessibility = "Flight details header")
    @iOSXCUITFindBy(accessibility = "Flight details header")
    private WebElement detailsHeader;

    @AndroidFindBy(accessibility = "Origin destination route")
    @iOSXCUITFindBy(accessibility = "Origin destination route")
    private WebElement routeLabel;

    @AndroidFindBy(accessibility = "Flight date")
    @iOSXCUITFindBy(accessibility = "Flight date")
    private WebElement dateLabel;

    @AndroidFindBy(accessibility = "Flight price")
    @iOSXCUITFindBy(accessibility = "Flight price")
    private WebElement priceLabel;

    // =========================================================================
    // Accessors
    // =========================================================================

    public String getRoute() {
        return getText(routeLabel);
    }

    public String getDate() {
        return getText(dateLabel);
    }

    public String getPrice() {
        return getText(priceLabel);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(detailsHeader);
    }
}
