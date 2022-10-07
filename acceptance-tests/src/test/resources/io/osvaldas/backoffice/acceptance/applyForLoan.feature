Feature: User can take a loan

  Scenario: Apply for successful loan
    Given client is registered
    And client is activated

    When loan is taken with amount 100
    Then loan is given

    When extension is taken
    Then extension is given