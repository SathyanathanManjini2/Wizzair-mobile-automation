@resilience @background-resume
Feature: App Background and Resume Preserves Form Data
  As a traveller filling in passenger details
  When I accidentally leave the app and return
  I want my entered data to still be present
  So that I do not need to re-enter everything

  @android @ios
  Scenario: Data is preserved after app is sent to background for 30 seconds
    Given I am on the passenger details screen for flight "LTN" to "BCN"
    When I enter my passenger details:
      | First Name | Last Name | Email                | Phone        |
      | Alice      | Walker    | alice@example.com    | +44111222333 |
    And I send the app to the background for 30 seconds
    And I resume the app
    Then the first name field should still contain "Alice"
    And the last name field should still contain "Walker"
    And the email field should still contain "alice@example.com"

  @android @ios
  Scenario: Long background (60s) still preserves form data
    Given I am on the passenger details screen for flight "BUD" to "LTN"
    When I enter my passenger details:
      | First Name | Last Name | Email              | Phone        |
      | Bob        | Jones     | bob@example.com    | +44999888777 |
    And I send the app to the background for 60 seconds
    And I resume the app
    Then the first name field should still contain "Bob"
