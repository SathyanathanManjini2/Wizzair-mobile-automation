package com.wizzair.pages.common;

import com.wizzair.config.ConfigLoader;
import com.wizzair.config.DeviceConfig;
import com.wizzair.core.driver.DriverManager;
import com.wizzair.core.wait.WaitStrategy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Base class for all Page Objects.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Initialises Appium PageFactory element decorators</li>
 *   <li>Exposes protected helpers (driver, config, wait) to subclasses</li>
 *   <li>Provides a {@link #isLoaded()} contract to verify page readiness</li>
 * </ul>
 *
 * <p>Subclasses must implement {@link #isLoaded()} to check the page's unique
 * identifier element (e.g. a header or title).
 */
public abstract class BasePage {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    protected final DeviceConfig config = ConfigLoader.load();

    protected BasePage() {
        // Initialise @FindBy / @iOSXCUITFindBy / @AndroidFindBy annotations
        PageFactory.initElements(
            new AppiumFieldDecorator(driver(), Duration.ofSeconds(15)),
            this
        );
    }

    // -------------------------------------------------------------------------
    // Contract
    // -------------------------------------------------------------------------

    /**
     * Verifies that the page has fully loaded.
     * Override in each page to check a unique element (e.g. page header).
     *
     * @return {@code true} when the page is ready
     */
    public abstract boolean isLoaded();

    // -------------------------------------------------------------------------
    // Protected convenience accessors
    // -------------------------------------------------------------------------

    protected AppiumDriver driver() {
        return DriverManager.getDriver();
    }

    protected void tap(WebElement element) {
        WaitStrategy.waitForClickable(element).click();
    }

    protected void type(WebElement element, String text) {
        WaitStrategy.waitForClickable(element);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(WebElement element) {
        return WaitStrategy.waitForVisible(element).getText();
    }

    protected boolean isDisplayed(WebElement element) {
        return WaitStrategy.isVisible(element);
    }
}
