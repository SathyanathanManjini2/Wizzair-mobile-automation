@smoke @deep-link
Feature: Deep Link Launch and First-Run Permissions
  As a traveller
  I want to open the WizzAir app via a deep link to a specific flight
  So that I can quickly see the details of that flight

  Background:
    Given the WizzAir app is launched fresh (no cached state)

  @android @ios
  Scenario: Launch via deep link and verify flight details screen
    Given I open the app using the deep link for flight "LTN" to "BCN" on "2025-07-15"
    When all system permission dialogs are handled
    Then the flight details screen should be displayed
    And the route shown should be "London Luton → Barcelona"
    And the date shown should be "15 Jul 2025"

  @android @ios
  Scenario: Deep link to a one-way flight shows correct departure details
    Given I open the app using the deep link for flight "BUD" to "CDG" on "2025-08-20"
    When all system permission dialogs are handled
    Then the flight details screen should be displayed
    And the route shown should be "Budapest → Paris Charles de Gaulle"
