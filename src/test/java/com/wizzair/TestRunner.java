package com.wizzair;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * JUnit Platform Suite test runner for Cucumber.
 *
 * <p>Run all tests:
 * <pre>
 *   mvn test -Dplatform=android
 * </pre>
 *
 * <p>Run only smoke tests:
 * <pre>
 *   mvn test -Dplatform=android -Dcucumber.filter.tags="@smoke"
 * </pre>
 *
 * <p>Run only specific feature:
 * <pre>
 *   mvn test -Dcucumber.features="src/test/resources/features/01_deep_link_permissions.feature"
 * </pre>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.wizzair.steps,com.wizzair.hooks")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm, "
              + "json:target/cucumber-reports/cucumber.json, "
              + "html:target/cucumber-reports/cucumber.html")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME,
        value = "not @wip")   // skip work-in-progress scenarios by default
@ConfigurationParameter(key = PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME,
        value = "false")      // set to "true" for parallel; requires separate Appium sessions
public class TestRunner {
    // Entry point discovered by JUnit Platform – no body required
}
