Feature: Update a todo
  As a user, I want to update a todo so that I can keep its information accurate.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - replace an existing todo successfully
    Given a todo exists with title "<originalTitle>", description "<originalDescription>", and done status "<originalDoneStatus>"
    When I replace the todo with title "<updatedTitle>", description "<updatedDescription>", and done status "<updatedDoneStatus>"
    Then the response status should be 200
    And the updated todo should have title "<updatedTitle>"
    And the updated todo should have description "<updatedDescription>"
    And the updated todo should be marked as <updatedDoneState>
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should have title "<updatedTitle>"
    And the retrieved todo should have description "<updatedDescription>"
    And the retrieved todo should be marked as <updatedDoneState>

    Examples:
      | originalTitle    | originalDescription   | originalDoneStatus | updatedTitle         | updatedDescription        | updatedDoneStatus | updatedDoneState |
      | Plan sprint      | Draft sprint items    | false              | Plan sprint review   | Draft the review agenda   | true              | done             |
      | Review backlog   | Check pending stories | true               | Review release scope | Confirm the release scope | false             | not done         |

  Scenario Outline: Alternate flow - partially update only the description using the supported amend behavior
    Given a todo exists with title "<title>", description "<originalDescription>", and done status "<doneStatus>"
    When I partially update only the todo description to "<updatedDescription>"
    Then the response status should be 200
    And the updated todo should have title "<title>"
    And the updated todo should have description "<updatedDescription>"
    And the updated todo should be marked as <doneState>
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should have title "<title>"
    And the retrieved todo should have description "<updatedDescription>"
    And the retrieved todo should be marked as <doneState>

    Examples:
      | title         | originalDescription | doneStatus | updatedDescription        | doneState |
      | Buy groceries | Milk and bread      | false      | Milk, bread, and eggs     | not done  |
      | Plan trip     | Book train tickets  | true       | Book train and hotel      | done      |

  Scenario Outline: Error flow - reject an update with invalid data
    Given a todo exists with title "<title>", description "<description>", and done status "<doneStatus>"
    When I try to replace the todo with an empty title
    Then the response status should be 400
    And the error message should mention "title"
    When I retrieve the todo by id
    Then the response status should be 200
    And the retrieved todo should have title "<title>"
    And the retrieved todo should have description "<description>"

    Examples:
      | title          | description             | doneStatus |
      | Prepare slides | Draft the introduction  | false      |
      | Submit budget  | Add the final numbers   | true       |
