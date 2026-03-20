Feature: Associate a todo with a category
  As a user, I want to associate a todo with a project or category so that I can organize my tasks.

  Background:
    Given the Todo Manager API is running

  Scenario Outline: Normal flow - link an existing todo to an existing category
    Given a todo exists with title "<todoTitle>", description "<todoDescription>", and done status "<todoDoneStatus>"
    And a category exists with title "<categoryTitle>" and description "<categoryDescription>"
    When I link the todo to the category
    Then the response status should be 201
    When I retrieve the categories for the todo
    Then the response status should be 200
    And the retrieved categories should include the linked category

    Examples:
      | todoTitle                  | todoDescription       | todoDoneStatus | categoryTitle | categoryDescription |
      | Plan ECSE 429 deliverables | Organize the work     | false          | School        | ECSE 429            |
      | Prepare camping checklist  | Gather the essentials | true           | Personal      | Weekend trip        |

  Scenario Outline: Alternate flow - retrieve the category relationship after creating it
    Given a todo exists with title "<todoTitle>", description "<todoDescription>", and done status "<todoDoneStatus>"
    And a category exists with title "<categoryTitle>" and description "<categoryDescription>"
    When I link the todo to the category
    Then the response status should be 201
    When I retrieve the categories for the todo
    Then the response status should be 200
    And the retrieved categories should contain title "<categoryTitle>"
    And the retrieved categories should contain description "<categoryDescription>"

    Examples:
      | todoTitle          | todoDescription          | todoDoneStatus | categoryTitle | categoryDescription |
      | Book winter tires  | Before the first snowfall| false          | Errands       | Weekend             |
      | Review lecture 5   | Finish before Thursday   | true           | School        | Weekly prep         |

  Scenario Outline: Error flow - reject a link to a non-existent category
    Given a todo exists with title "<todoTitle>", description "<todoDescription>", and done status "<todoDoneStatus>"
    When I try to link the todo to the non-existent category with id "<missingCategoryId>"
    Then the response status should be 404
    When I retrieve the categories for the todo
    Then the response status should be 200
    And the retrieved categories should be empty

    Examples:
      | todoTitle           | todoDescription | todoDoneStatus | missingCategoryId |
      | Sort tax documents  | For next month  | false          | 99999999          |
      | Archive email       | Cleanup inbox   | true           | 88888888          |
