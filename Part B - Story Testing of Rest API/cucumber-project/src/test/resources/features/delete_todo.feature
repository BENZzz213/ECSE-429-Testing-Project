Feature: Delete a todo
  As a user, I want to delete a todo so that I can remove tasks I no longer need.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - delete an existing todo
    Given a todo exists with title "<title>", description "<description>", and done status "<doneStatus>"
    When I delete the todo
    Then the response status should be 200
    When I retrieve the todo by id
    Then the response status should be 404

    Examples:
      | title                       | description       | doneStatus |
      | Cancel dentist appointment  | No longer needed  | false      |
      | Remove draft reminder       | It is obsolete    | true       |

  Scenario Outline: Alternate flow - create and then delete a todo in the same scenario
    When I create a todo with title "<title>", description "<description>", and done status "<doneStatus>"
    Then the response status should be 201
    When I delete the created todo
    Then the response status should be 200
    When I retrieve the created todo by id
    Then the response status should be 404

    Examples:
      | title            | description            | doneStatus |
      | Clean the inbox  | Archive old messages   | false      |
      | Wash the car     | Before the weekend     | true       |

  Scenario Outline: Error flow - delete the same todo twice
    Given a todo exists with title "<title>", description "<description>", and done status "<doneStatus>"
    When I delete the todo
    Then the response status should be 200
    When I delete the same todo again
    Then the response status should be 404
    And retrieving the deleted todo by id should return status 404

    Examples:
      | title                | description     | doneStatus |
      | Return library book  | Due tomorrow    | false      |
      | Cancel old booking   | Already handled | true       |
