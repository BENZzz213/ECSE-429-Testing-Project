# Part B - Story Testing of Rest API

This folder is a standalone Maven + Cucumber project for the Todo Manager REST API story tests.

## Where to add the feature files

Copy your five `.feature` files into:

`src/test/resources/features/`

Expected filenames:

- `create_todo.feature`
- `update_todo.feature`
- `todo_status.feature`
- `delete_todo.feature`
- `todo_relationships.feature`

## Project structure

- `pom.xml` Maven build for the Cucumber suite
- `src/test/java/ecse429/bdd/RunCucumberTest.java` JUnit Platform entry point
- `src/test/java/ecse429/bdd/steps/TodoStepDefinitions.java` step definitions
- `src/test/java/ecse429/bdd/support/` shared API helpers and config
- `src/test/resources/features/` Gherkin feature files

## Note on Scenario Outline variables

Some feature files use an Examples column named `doneState`. This is not an API field.
It is only a Gherkin placeholder used to keep assertion steps readable in `Scenario Outline` tests.

- `doneStatus` is the actual API value sent in requests: `true` or `false`
- `doneState` is the expected natural-language assertion text: `done` or `not done`

For example, an Examples row may contain `doneStatus = true` and `doneState = done`.
This lets the same outline express both request data and readable expected outcomes without changing the step-definition wording.

## Run

Start the Todo Manager API first, then run:

```powershell
mvn test
```

If the API is not running at `http://localhost:4567`, override the base URL:

```powershell
mvn -Dtodo.baseUrl=http://localhost:4567 test
```
