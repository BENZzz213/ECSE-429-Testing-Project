Feature: Associate a todo with a category
  As a user, I want to associate a todo with a project or category so that I can organize my tasks.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal flow - link an existing todo to an existing category
    Given a todo exists with title "Plan ECSE 429 deliverables", description "Organize the work", and done status "false"
    And a category exists with title "School" and description "ECSE 429"
    When I link the todo to the category
    Then the response status should be 201
    When I retrieve the categories for the todo
    Then the response status should be 200
    And the retrieved categories should include the linked category

  Scenario: Alternate flow - retrieve the category relationship after creating it
    Given a todo exists with title "Book winter tires", description "Before the first snowfall", and done status "false"
    And a category exists with title "Errands" and description "Weekend"
    When I link the todo to the category
    Then the response status should be 201
    When I retrieve the categories for the todo
    Then the response status should be 200
    And the retrieved categories should contain title "Errands"
    And the retrieved categories should contain description "Weekend"

  Scenario: Error flow - reject a link to a non-existent category
    Given a todo exists with title "Sort tax documents", description "For next month", and done status "false"
    When I try to link the todo to the non-existent category with id "99999999"
    Then the response status should be 404
    When I retrieve the categories for the todo
    Then the response status should be 200
    And the retrieved categories should be empty
