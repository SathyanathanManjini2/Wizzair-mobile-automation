@booking @price-change
Feature: Price Change Modal During Booking
  As a traveller who has selected a flight
  When the price changes during the booking flow
  I want to be informed and have the option to accept the new price or go back

  Background:
    Given I have selected the flight from "LTN" to "BCN" departing at "06:00"

  @android @ios
  Scenario: Price change modal appears and user accepts the new price
    Given I am on the passenger details screen
    When I enter my passenger details:
      | First Name | Last Name | Email                  | Phone       |
      | John       | Doe       | john.doe@example.com   | +44123456789|
    And a price change modal appears during the booking flow
    Then I should see the new price displayed in the modal
    When I accept the new price
    Then the modal should close
    And I should be able to continue the booking process

  @android @ios
  Scenario: Booking continues normally if no price change occurs
    Given I am on the passenger details screen
    When I enter my passenger details:
      | First Name | Last Name | Email                  | Phone       |
      | Jane       | Smith     | jane.smith@example.com | +44987654321|
    And no price change modal appears
    Then I should proceed directly to the payment screen
