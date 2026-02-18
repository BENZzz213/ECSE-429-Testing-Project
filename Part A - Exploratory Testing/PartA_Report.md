# ECSE 429 Part A Report – Exploratory Testing of REST API (Todo Manager)

**Course:** ECSE 429 – Software Validation (Winter 2026)  
**Part:** A – Exploratory Testing of REST API  
**Team:** Benzaid Mohamed-Amine  
**Student ID:** 261120610  
**Email:** mohamed-amine.benzaid@mail.mcgill.ca  
**Date:** 2026-02-17  

---

## Repository Overview

This submission repository contains all Part A artifacts in the `Exploratory Testing/` folder:

- `scripts/` ? exploratory testing scripts and session demos
- `Unit Tests Suit/` ? JUnit test suite (Java + Maven)
- `Session Notes.md` and `Session Notes.pdf` ? exploratory session notes
- `Bug_Report.md` and `Bug_Report.pdf` ? bug report and evidence
- `Todo_API_Exploratory.postman_collection.json` ? Postman collection used during exploration
- `PartA_Report.md` ? written report (this document)

## Executive Summary

This report documents the exploratory testing activities conducted on the Todo Manager REST API, as well as the supporting scripts, bug findings, and unit test suite built from the discoveries. Three timeboxed exploratory sessions were executed to identify documented and undocumented behavior, validate core CRUD capabilities, and explore relationship endpoints and edge cases. The testing effort focused on todo-related endpoints, consistent with the guidance for a single-member team. The deliverables include session notes in PDF/MD format, exploratory scripts and Postman collections, a bug report (MD/PDF), and a JUnit-based unit test suite. The testing uncovered multiple inconsistencies between the documented and actual behavior, particularly around error codes and relationship endpoints, as well as several data integrity issues (duplicate relationships returned for non-existent todos). The unit test suite includes separate modules for documented and undocumented behaviors, and an XML payload validation module, as required.

---

## Application Under Test

The application under test is the Todo Manager REST API running locally on `http://localhost:4567`. The API exposes endpoints for todos and their relationships to categories and projects. The Swagger/OpenAPI specification in the repository was used as the primary documentation source, and the local documentation endpoint `/docs` was referenced during testing.

---

## Session Summaries

### Session 1 – Core CRUD + Return Codes

**Key capabilities confirmed:**
- `GET /todos` returns the list of todos.
- `POST /todos` creates a new todo (201 Created).
- `GET /todos/:id`, `PUT /todos/:id`, `POST /todos/:id`, and `DELETE /todos/:id` operate on single todos.
- `HEAD` and `OPTIONS` endpoints return headers and allowed methods.
- `PUT/DELETE/PATCH` on `/todos` return 405 (method not allowed).

**Notable observations:**
- `POST /todos` correctly rejects malformed JSON and missing required fields.
- `PUT /todos/:id` requires `title`, and empty title is rejected.
- After delete, a second delete returns 404 (expected invalid operation).

### Session 2 – Relationships + Side Effects

**Key capabilities confirmed:**
- `GET /todos/:id/categories` and `POST /todos/:id/categories` manage todo–category relationships.
- `GET /todos/:id/tasksof` and `POST /todos/:id/tasksof` manage todo–project relationships.
- Relationship deletion via `DELETE /todos/:id/categories/:id` and `DELETE /todos/:id/tasksof/:id` works for linked objects.

**Notable observations:**
- Some relationship endpoints return error codes that differ from Swagger (404 instead of 405/400).
- Relationships allow multiple categories for a todo without overwriting existing links.

### Session 3 – Validation, Edge Cases & Content Negotiation

**Key capabilities confirmed:**
- Validation of required fields and types (`doneStatus` must be boolean).
- API accepts JSON and XML payloads for core todo operations.

**Notable bugs found:**
- Duplicate categories/projects returned for non-existent todo IDs.
- Non-existent relationship deletions produce internal error messages rather than clean 4xx responses.
- `PUT /todos/:id` with only `title` resets other fields (potential data loss).

---

## Capabilities Identified (Todos Scope)

Documented APIs tested and covered in the unit test suite:
- `/todos`
- `/todos/:id`
- `/todos/:id/categories`
- `/todos/:id/categories/:id`
- `/todos/:id/tasksof`
- `/todos/:id/tasksof/:id`

Undocumented behaviors verified and separated into a dedicated module:
- 404 responses where Swagger documents 405 or 400.
- Relationship error cases returning 404 instead of 400.
- Side effects and inconsistent update semantics (`PUT` resets fields, `POST` ignores id field).

---

## Scripts Demonstrating Capabilities

Exploratory scripts are stored in:
- `Exploratory Testing/scripts/`

The scripts demonstrate:
- CRUD operations for todos.
- Relationship creation and deletion for categories and tasksof.
- Error handling and edge cases (invalid payloads, non-existent IDs).

The Postman collection used during exploration is:
- `Todo_API_Exploratory.postman_collection.json`

---

## Bug Summary

A detailed bug report is provided in:
- `Bug_Report.md`
- `Bug_Report.pdf`

High-level bug themes:
- Mismatches between Swagger error codes (400/405) and actual 404 behavior.
- Duplicate relationship data returned for non-existent todos.
- Data loss risk when using `PUT` with partial fields.

---

## Unit Test Suite
### Unit Test Execution Results

- Command: `mvn test`
- Result: All tests passed (documented mismatch tests are marked `@Disabled`).
- Notes: The screenshot below shows the successful run.

![mvn test results](<images/test-execution.png>)

The unit test suite is located in:
- `Unit Tests Suit/`

**Tooling:**
- Java + JUnit 5 + Maven
- HTTP calls executed directly against the live API (no mocks)

**Structure (per API group):**
- `TodosCollectionTests`
- `TodoItemTests`
- `TodoCategoriesCollectionTests`
- `TodoCategoryRelationshipTests`
- `TodoTasksofCollectionTests`
- `TodoTasksofRelationshipTests`
- `TodoInvalidInputTests`
- `TodoXmlPayloadTests`
- `ServiceAvailabilityTests`
- `TodoUndocumentedBehaviorTests`

**Key requirements satisfied:**
- Tests run in random order.
- Each test creates and cleans its own data when possible.
- JSON and XML payload support verified.
- Malformed JSON and XML tests included.
- Invalid operations included (e.g., delete twice).
- Documented vs undocumented behavior split into separate modules.

**Documented vs Undocumented:**
Where the API differs from the Swagger spec, the suite contains:
- **Documented tests** asserting expected behavior (in API-specific modules).
- **Undocumented tests** asserting actual behavior (in `TodoUndocumentedBehaviorTests`).

For some documented 400 cases that are currently returning 404, documented tests are included but marked `@Disabled` to keep the suite green, with explanations in the test code and README.

---
