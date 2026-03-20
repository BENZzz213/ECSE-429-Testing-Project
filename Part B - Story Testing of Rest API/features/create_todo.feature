Feature: Create a todo
  As a user, I want to create a todo so that I can track a task.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal flow - create a todo with valid required fields
    When I create a todo with title "Buy groceries"
    Then the response status should be 201
    And the created todo should have title "Buy groceries"
    And the created todo should be marked as not done
    And the created todo should have an empty description
    When I retrieve the created todo by id
    Then the response status should be 200
    And the retrieved todo should have title "Buy groceries"
    And the retrieved todo should be marked as not done

  Scenario: Alternate flow - create a todo with additional optional fields
    When I create a todo with title "Submit lab report", description "Upload the PDF before 5 PM", and done status "true"
    Then the response status should be 201
    And the created todo should have title "Submit lab report"
    And the created todo should have description "Upload the PDF before 5 PM"
    And the created todo should be marked as done
    When I retrieve the created todo by id
    Then the response status should be 200
    And the retrieved todo should have description "Upload the PDF before 5 PM"
    And the retrieved todo should be marked as done

  Scenario: Error flow - reject a todo with a missing required title
    When I create a todo without a title and with description "Missing title"
    Then the response status should be 400
    And the response should not include a todo id
    And the error message should mention "title"
