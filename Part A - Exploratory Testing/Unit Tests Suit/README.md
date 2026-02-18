# Unit Test Suite (Todo Manager)

## Structure

All tests live in src/test/java/ecse429/todos and use JUnit 5. We organized tests by API group so each documented endpoint family has its own module:

- TodosCollectionTests → /todos
- TodoItemTests → /todos/:id
- TodoCategoriesCollectionTests → /todos/:id/categories
- TodoCategoryRelationshipTests → /todos/:id/categories/:id
- TodoTasksofCollectionTests → /todos/:id/tasksof
- TodoTasksofRelationshipTests → /todos/:id/tasksof/:id
- TodoInvalidInputTests → malformed payloads + invalid operations
- TodoXmlPayloadTests → XML payload support
- ServiceAvailabilityTests → service must be running
- TodoUndocumentedBehaviorTests → observed behaviors that diverge from Swagger

Shared helpers live in src/test/java/ecse429/support/TestSupport.java.

## Documented vs Undocumented Behavior

The project requires **two separate modules** when actual behavior differs from documentation:

- **Documented tests** assert what Swagger says *should* happen.
- **Undocumented tests** assert what the system *actually* does.

Some documented tests for error codes (expected 400, actual 404) are present but marked @Disabled so the suite stays green. The disabled tests are:

- postTodoCategoriesMissingTodoReturns400_Documented
- postTodoCategoriesMissingCategoryReturns400_Documented
- postTodoTasksofMissingTodoReturns400_Documented
- postTodoTasksofMissingProjectReturns400_Documented

The **actual 404 behavior** is verified in TodoUndocumentedBehaviorTests.

If you want to demonstrate the failure explicitly, remove @Disabled on those tests.

## Running

- Run all tests with Maven:
  - mvn test
- Run a single class:
  - mvn -Dtest=TodosCollectionTests test

## Notes

- Tests use real HTTP calls to http://localhost:4567.
- Each test creates and cleans up its own data when possible, so tests can run in any order.
