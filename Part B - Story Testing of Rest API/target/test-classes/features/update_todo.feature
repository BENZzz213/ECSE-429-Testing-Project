Feature: Update a todo
  As a user, I want to update a todo so that I can keep its information accurate.

  Background:
    Given the Todo Manager API is running

  Scenario: Normal flow - replace an existing todo successfully
    Given a todo exists with title "Plan sprint", description "Draft sprint items", and done status "false"
    When I replace the todo with title "Plan sprint review", description "Draft the review agenda", and done status "true"
    Then the response status should be 200
    And the updated todo should have title "Plan sprint review"
    And the updated todo should have description "Draft the review agenda"
    And the updated todo should be marked as done
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should have title "Plan sprint review"
    And the retrieved todo should have description "Draft the review agenda"
    And the retrieved todo should be marked as done

  Scenario: Alternate flow - partially update only the description using the supported amend behavior
    Given a todo exists with title "Buy groceries", description "Milk and bread", and done status "false"
    When I partially update only the todo description to "Milk, bread, and eggs"
    Then the response status should be 200
    And the updated todo should have title "Buy groceries"
    And the updated todo should have description "Milk, bread, and eggs"
    And the updated todo should be marked as not done
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should have title "Buy groceries"
    And the retrieved todo should have description "Milk, bread, and eggs"
    And the retrieved todo should be marked as not done

  Scenario: Error flow - reject an update with invalid data
    Given a todo exists with title "Prepare slides", description "Draft the introduction", and done status "false"
    When I try to replace the todo with an empty title
    Then the response status should be 400
    And the error message should mention "title"
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should have title "Prepare slides"
    And the retrieved todo should have description "Draft the introduction"
