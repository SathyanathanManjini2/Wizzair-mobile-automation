@payment @webview @e2e
Feature: Complete Payment Inside a WebView
  As a traveller who has completed passenger details
  I want to securely pay for my flight inside an embedded payment form
  So that my booking is confirmed end-to-end

  Background:
    Given I have navigated to the payment screen for flight "LTN" to "BCN"

  @android @ios
  Scenario: Complete E2E payment flow via WebView form
    Given the payment form is loaded inside a WebView
    When I fill in the card details:
      | Card Number      | Expiry | CVV | Card Holder |
      | 4111111111111111 | 12/26  | 123 | John Doe    |
    And I submit the payment
    Then the payment should be processed successfully
    And I should be returned to the native app context
    And a booking confirmation should be displayed
