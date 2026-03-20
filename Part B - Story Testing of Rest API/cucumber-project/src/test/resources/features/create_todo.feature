Feature: Create a todo
  As a user, I want to create a todo so that I can track a task.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - create a todo with valid required fields
    When I create a todo with title "<title>"
    Then the response status should be 201
    And the created todo should have title "<title>"
    And the created todo should be marked as not done
    And the created todo should have an empty description
    When I retrieve the created todo by id
    Then the response status should be 200
    And the retrieved todo should have title "<title>"
    And the retrieved todo should be marked as not done

    Examples:
      | title               |
      | Buy groceries       |
      | Schedule dentist    |

  Scenario Outline: Alternate flow - create a todo with additional optional fields
    When I create a todo with title "<title>", description "<description>", and done status "<doneStatus>"
    Then the response status should be 201
    And the created todo should have title "<title>"
    And the created todo should have description "<description>"
    And the created todo should be marked as <doneState>
    When I retrieve the created todo by id
    Then the response status should be 200
    And the retrieved todo should have description "<description>"
    And the retrieved todo should be marked as <doneState>

    Examples:
      | title             | description                  | doneStatus | doneState |
      | Submit lab report | Upload the PDF before 5 PM   | true       | done      |
      | Call the bank     | Confirm the replacement card | false      | not done  |

  Scenario Outline: Error flow - reject a todo with a missing required title
    When I create a todo without a title and with description "<description>"
    Then the response status should be 400
    And the response should not include a todo id
    And the error message should mention "title"

    Examples:
      | description            |
      | Missing title          |
      | No title was provided  |
