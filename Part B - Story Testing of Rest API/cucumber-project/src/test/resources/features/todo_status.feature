Feature: Mark a todo as done or not done
  As a user, I want to mark a todo as done or not done so that I can track my progress.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - mark an incomplete todo as done
    Given a todo exists with title "<title>", description "<description>", and done status "false"
    When I mark the todo as done
    Then the response status should be 200
    And the updated todo should be marked as done
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should be marked as done

    Examples:
      | title            | description     |
      | Submit timesheet | Before Friday   |
      | Pay electricity  | Before the 20th |

  Scenario Outline: Alternate flow - mark a done todo back to not done
    Given a todo exists with title "<title>", description "<description>", and done status "true"
    When I mark the todo as not done
    Then the response status should be 200
    And the updated todo should be marked as not done
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should be marked as not done

    Examples:
      | title             | description       |
      | Submit expenses   | Already filed     |
      | Backup laptop     | Completed earlier |

  Scenario Outline: Error flow - try to change the status of a non-existent todo
    Given a todo exists with title "<title>", description "<description>", and done status "false"
    And I delete the todo
    When I mark the todo as done
    Then the response status should be 404
    And the error message should mention "todo"

    Examples:
      | title             | description       |
      | Archive receipts  | Already removed   |
      | Close old ticket  | No longer needed  |
