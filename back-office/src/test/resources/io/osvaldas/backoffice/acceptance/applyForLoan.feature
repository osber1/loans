Feature: User can take a loan

  Scenario: Apply for successful loan
    Given client is created
    And client is active
    When loan is taken with amount 100
    Then loan is given