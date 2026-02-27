@smoke @search @infinite-scroll
Feature: Flight Search with Infinite Scroll
  As a traveller
  I want to search for flights and scroll through results
  So that I can find and select a specific flight

  @android @ios
  Scenario: Search for a flight and locate it via infinite scroll
    Given I am on the flight search screen
    When I search for a flight from "LTN" to "BCN" on "2025-07-15"
    Then the flight results list is displayed
    And I scroll until I find the flight departing at "06:00" and arriving at "08:30"
    And I should be able to tap that flight to begin booking

  @android @ios
  Scenario: Search returns flights for a high-demand route
    Given I am on the flight search screen
    When I search for a flight from "BUD" to "LTN" on "2025-12-23"
    Then the flight results list is displayed
    And the list contains at least 1 flight option
