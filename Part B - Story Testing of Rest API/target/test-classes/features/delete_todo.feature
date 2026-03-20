Feature: Delete a todo
  As a user, I want to delete a todo so that I can remove tasks I no longer need.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal flow - delete an existing todo
    Given a todo exists with title "Cancel dentist appointment", description "No longer needed", and done status "false"
    When I delete the todo
    Then the response status should be 200
    When I retrieve the todo by id
    Then the response status should be 404

  Scenario: Alternate flow - create and then delete a todo in the same scenario
    When I create a todo with title "Clean the inbox", description "Archive old messages", and done status "false"
    Then the response status should be 201
    When I delete the created todo
    Then the response status should be 200
    When I retrieve the created todo by id
    Then the response status should be 404

  Scenario: Error flow - delete the same todo twice
    Given a todo exists with title "Return library book", description "Due tomorrow", and done status "false"
    When I delete the todo
    Then the response status should be 200
    When I delete the same todo again
    Then the response status should be 404
    And retrieving the deleted todo by id should return status 404
