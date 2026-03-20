# ECSE 429 Part B Report - Story Testing of REST API (Todo Manager)

**Course:** ECSE 429 - Software Validation (Winter 2026)  
**Part:** B - Story Testing of REST API  
**Team:** Benzaid Mohamed-Amine  
**Student ID:** 261120610  
**Email:** mohamed-amine.benzaid@mail.mcgill.ca  
**Date:** 2026-03-20  
**Repo Link:** https://github.com/BENZzz213/ECSE-429-Testing-Project/tree/main

---

## Repository Overview

This submission repository contains all Part B artifacts in the `Part B - Story Testing of Rest API/` folder:

- `cucumber-project/` - standalone Maven + Cucumber project for the story tests
- `cucumber-project/src/test/resources/features/` - Gherkin feature files for the Todo Manager user stories
- `cucumber-project/src/test/java/ecse429/bdd/steps/` - reusable step definitions library
- `cucumber-project/src/test/java/ecse429/bdd/support/` - shared API client helpers, JSON utilities, and configuration
- `cucumber-project/README.md` - usage notes, execution instructions, and Scenario Outline variable notes
- `PartB_Report.md` - written report for Part B (this document)

Generated folders such as `cucumber-project/target/` and `cucumber-project/.m2/` are local build artifacts and dependency caches used to execute the suite. They are not part of the authored acceptance-test logic.

## Executive Summary

This report documents the story testing work completed for Part B of the Todo Manager REST API project. The scope for a single-member team was limited to todo-related functionality, consistent with the work done in Part A. Five user stories were selected around the core lifecycle of a todo: creation, update, completion status changes, deletion, and category association. For each story, three acceptance-test flows were defined: a normal flow, an alternate flow, and an error flow. The final suite therefore contains 15 Scenario Outline definitions and 30 concrete executions through two example rows per outline.

The story tests were automated using Cucumber with Java, JUnit Platform, and Maven. Feature files are written in Gherkin and executed against the live Todo Manager REST API running locally. Reusable step definitions and shared support utilities were implemented to keep the suite maintainable and consistent. The suite validates both successful behavior and failure behavior, cleans up scenario-created data after execution, and fails early if the target API service is unavailable.

Execution of the completed Part B suite confirmed the actual todo behavior already identified during Part A exploratory testing. All 30 scenario runs passed successfully against the local service. No new bugs were identified during Part B; instead, the story tests confirmed the previously observed actual behavior of the system, including known mismatches between documented and actual relationship error responses.

---

## Application Under Test

The application under test is the Todo Manager REST API running locally on `http://localhost:4567`. The Part B suite focuses only on the todos domain and one supported relationship path for organizing todos through categories. This scope matches the project requirement for an individual contributor and also aligns with the strongest todo-related behavior already explored and verified in Part A.

The Part B automation uses the actual behavior observed during Part A as its reference point. In particular, the story tests were written to reflect the system behavior that was confirmed experimentally, rather than idealized Swagger behavior where the documentation and implementation differ.

---

## Story Test Scope and Deliverables

### User Stories Implemented

The following five required user stories were implemented:

1. As a user, I want to create a todo so that I can track a task.
2. As a user, I want to update a todo so that I can keep its information accurate.
3. As a user, I want to mark a todo as done or not done so that I can track my progress.
4. As a user, I want to delete a todo so that I can remove tasks I no longer need.
5. As a user, I want to associate a todo with a project or category so that I can organize my tasks.

### Deliverables Produced

The Part B submission includes the following deliverables:

- Five feature files covering the five required stories
- Three acceptance-test flows per story: normal, alternate, and error
- Scenario Outline based Gherkin scripts with two example rows each
- A Cucumber automation project built with Maven and JUnit Platform
- A reusable step-definition library and shared support code
- A README describing the structure and execution of the suite
- The story test execution video demonstrating the suite and different execution orders
- This written report

### Feature Files

The story tests are organized into the following five feature files:

- `create_todo.feature` - creation behavior for todos
- `update_todo.feature` - full replacement and partial amend behavior for todos
- `todo_status.feature` - changing `doneStatus` between complete and incomplete
- `delete_todo.feature` - deletion and repeated deletion behavior
- `todo_relationships.feature` - linking todos to categories and retrieving the relationship

Each feature contains:

- One `Background` section for shared service availability setup
- Three `Scenario Outline` definitions
- Two `Examples` rows per outline

This yields 15 scenario outlines and 30 concrete scenario executions.

---

## Structure of the Story Test Suite

The Part B story test suite is implemented as a standalone Cucumber project inside:

- `Part B - Story Testing of Rest API/cucumber-project/`

### Automation Layers

The suite is organized into three main layers:

- **Feature layer:** Gherkin feature files stored in `src/test/resources/features/`
- **Glue layer:** step definitions stored in `src/test/java/ecse429/bdd/steps/`
- **Support layer:** reusable API and JSON helper code stored in `src/test/java/ecse429/bdd/support/`

### Main Source Files

The most important automation files are:

- `pom.xml` - Maven project definition and test dependencies
- `src/test/java/ecse429/bdd/RunCucumberTest.java` - JUnit Platform entry point for Cucumber
- `src/test/java/ecse429/bdd/steps/TodoStepDefinitions.java` - shared step definitions used across all feature files
- `src/test/java/ecse429/bdd/support/TestSupport.java` - HTTP request helpers, JSON parsing, and helper methods for creating/deleting entities
- `src/test/java/ecse429/bdd/support/TestConfig.java` - configuration such as the API base URL
- `src/test/resources/junit-platform.properties` - JUnit/Cucumber runtime configuration

### Tooling

The project uses the following tools and libraries:

- Java 21
- Maven
- Cucumber 7.14.0
- JUnit Platform 1.10.2
- JUnit Jupiter 5.10.2
- Jackson 2.17.2

### Reusability and Clean Structure

The suite was designed so that step definitions can be reused across stories instead of duplicating HTTP logic in each scenario. The feature files remain user-goal oriented, while the Java step definitions translate those high-level steps into concrete REST calls.

Examples of reused behavior include:

- creating a todo with common setup steps
- retrieving a todo by id after creation or update
- asserting response codes and persisted field values
- creating categories and linking them to todos
- deleting created entities during cleanup

A small note on the Scenario Outline design: some feature files use a placeholder named `doneState` in the `Examples` table. This is not an API field. It is only a Gherkin variable used to keep assertion steps readable as `done` or `not done`, while the actual request payload continues to use the API field `doneStatus` with `true` or `false`.

### Order Independence and Cleanup

The suite was written so that scenarios can run independently of one another:

- each scenario creates the data it needs
- each scenario stores created todo and category IDs locally
- an `@After` hook removes created data at the end of the scenario
- no scenario depends on pre-existing state from another scenario

The suite also includes a service-availability guard through the `Given the Todo Manager API is running` step. If the local API is not available, the scenario fails immediately rather than producing misleading downstream failures.

---

## Story Test Execution Results

### Execution Command

The suite was executed from the Cucumber project directory using:

```powershell
mvn test
```

The API was started locally before the test run.

### Results

![mvn test results](<images/test-execution.png>)

The final run of the completed suite produced the following result:

- 30 scenarios passed
- 248 steps passed
- 0 failures
- 0 errors
- 0 skipped tests

The suite passed successfully against the live Todo Manager REST API running on `http://localhost:4567`.

### Requirements Satisfied by Execution

The executed suite satisfies the main Part B automation requirements:

- uses Cucumber to parse and execute Gherkin feature files
- implements reusable step definitions as a shared Java library
- includes a `Background` section in each feature file
- uses `Scenario Outline` with variable data and `Examples` tables
- checks both success and failure behavior
- restores created data after each scenario
- is designed to run in any order because scenarios manage their own setup and teardown
- fails early if the service is unavailable

---

## Findings of Story Test Suite Execution

The story tests confirmed the following behavior for the Todo Manager API.

### Story 1 - Create a Todo

The API supports minimal todo creation with only the required title field, returning `201 Created`. It also supports creation with optional fields such as `description` and `doneStatus`. Invalid creation requests that omit the required title are rejected with `400 Bad Request`.

### Story 2 - Update a Todo

The API supports replacing an existing todo through full update behavior. It also supports partial amendment of certain fields using the supported amend path already observed in Part A. Invalid update data such as an empty title is rejected, and the original persisted todo remains unchanged after the failed request.

### Story 3 - Mark a Todo as Done or Not Done

The API correctly persists status transitions from not done to done and from done back to not done. Retrieval after each update confirms that the changed `doneStatus` value is stored by the system. Attempting to change the status of a deleted or non-existent todo returns `404`.

### Story 4 - Delete a Todo

Deleting an existing todo returns `200`, and a subsequent retrieval confirms that the resource no longer exists. Attempting to delete the same todo again returns `404`, which is consistent with the invalid-operation behavior already confirmed in Part A.

### Story 5 - Associate a Todo with a Category

The API supports linking an existing todo to an existing category, and the resulting relationship can be retrieved successfully. The relationship is persisted and visible through the todo-to-categories endpoint. Attempting to create the relationship using a non-existent category ID returns `404`, which matches the actual observed behavior from Part A.

### Overall Observations

Overall, the Part B story test suite confirmed that the todo-focused functionality explored in Part A is stable for the selected user stories. The acceptance tests provide end-to-end behavioral coverage at the user-story level while remaining grounded in actual REST API behavior.

The most important cross-part finding is that Part B confirms the actual system behavior already discovered during Part A. In particular:

- creation, update, deletion, and category-linking flows are all operational
- status changes persist correctly
- relationship error cases still return the actual `404` behavior seen in Part A
- `PUT` behavior remains aligned with the full-replacement semantics identified during exploratory testing

No additional bugs were identified during Part B execution.

---

## Additional Bug Summary

No new bugs were discovered during the execution of the Part B story test suite.

The main inconsistencies relevant to Part B were already known from Part A and were intentionally reflected in the acceptance tests so that the suite validates actual system behavior rather than failing on previously documented mismatches. The clearest example is the `404` response returned for invalid todo-category relationship requests, where documentation and implementation were previously found to differ.

---

## Conclusion

Part B produced a complete story test suite for the Todo Manager REST API focused on the todos domain. The final suite includes five required user stories, 15 scenario outlines, reusable Cucumber step definitions, a clean Maven-based project structure, and successful execution results against the live service.

The acceptance tests complement the lower-level testing work from Part A by validating realistic user-goal oriented behavior from end to end. Together, Parts A and B provide both exploratory and automated behavioral evidence for the todo portion of the API.

