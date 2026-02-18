# Bug Report - Todo Manager (Part A)

This document summarizes bugs identified from exploratory sessions and verified with unit tests. It also includes the required bug report form template.

## Bugs Found

### Bug 1
- **Executive Summary:** POST /todos/:id/categories returns 404 not 400
- **Description of Bug:** Swagger documents 400 for errors, but API returns 404 when the todo or category does not exist.
- **Potential Impact on System:** Client error handling based on docs will misclassify failures.
- **Steps to Reproduce:**
  1. POST /todos/99999999/categories with body {"id":"1"}.
  2. POST /todos/{validTodo}/categories with body {"id":"99999999"}.
- **Actual Result:** 404 Not Found.
- **Expected Result (per documentation):** 400 Bad Request.

### Bug 2
- **Executive Summary:** /todos/:id/categories/:id returns 404 not 405
- **Description of Bug:** Swagger says 405 (method not allowed), but GET/POST/HEAD return 404.
- **Potential Impact on System:** Clients expecting 405 may mis-handle errors.
- **Steps to Reproduce:**
  1. GET /todos/1/categories/1.
  2. POST /todos/1/categories/1.
  3. HEAD /todos/1/categories/1.
- **Actual Result:** 404 Not Found.
- **Expected Result (per documentation):** 405 Method Not Allowed.

### Bug 3
- **Executive Summary:** POST /todos/:id/tasksof returns 404 not 400
- **Description of Bug:** Swagger documents 400 for errors, but API returns 404 when todo or project does not exist.
- **Potential Impact on System:** Error handling based on docs breaks or misclassifies failures.
- **Steps to Reproduce:**
  1. POST /todos/99999999/tasksof with body {"id":"1"}.
  2. POST /todos/{validTodo}/tasksof with body {"id":"99999999"}.
- **Actual Result:** 404 Not Found.
- **Expected Result (per documentation):** 400 Bad Request.

### Bug 4
- **Executive Summary:** /todos/:id/tasksof/:id returns 404 not 405
- **Description of Bug:** Swagger says 405, but GET/POST/HEAD return 404.
- **Potential Impact on System:** Clients expecting 405 may mis-handle errors.
- **Steps to Reproduce:**
  1. GET /todos/1/tasksof/1.
  2. POST /todos/1/tasksof/1.
  3. HEAD /todos/1/tasksof/1.
- **Actual Result:** 404 Not Found.
- **Expected Result (per documentation):** 405 Method Not Allowed.

### Bug 5
- **Executive Summary:** GET /todos/1000/categories returns duplicates
- **Description of Bug:** For a non-existent todo, the endpoint returns duplicated categories instead of 404 or empty list.
- **Potential Impact on System:** Misleading data; clients may assume valid relationships exist.
- **Steps to Reproduce:**
  1. GET /categories and note category ids (e.g., 1 and 2).
  2. GET /todos/1000 returns 404.
  3. GET /todos/1000/categories returns duplicated category entries.
- **Actual Result:** Duplicate category objects in the response.
- **Expected Result (per documentation):** 404 Not Found or an empty list.

### Bug 6
- **Executive Summary:** GET /todos/1000/tasksof returns duplicates
- **Description of Bug:** For a non-existent todo, the endpoint returns duplicated projects instead of 404 or empty list.
- **Potential Impact on System:** Misleading data; clients may assume valid relationships exist.
- **Steps to Reproduce:**
  1. GET /todos/1000 returns 404.
  2. GET /todos/1000/tasksof returns repeated project entries.
- **Actual Result:** Duplicate project objects in the response.
- **Expected Result (per documentation):** 404 Not Found or an empty list.

### Bug 7
- **Executive Summary:** POST /todos/:id ignores id field in body
- **Description of Bug:** Sending { "id": 100 } to POST /todos/:id returns 200 but does not update id.
- **Potential Impact on System:** Ambiguous update semantics; clients may think the id changed.
- **Steps to Reproduce:**
  1. Create a todo.
  2. POST /todos/{id} with body { "id": 100 }.
- **Actual Result:** 200 OK, id remains unchanged.
- **Expected Result (per documentation):** Either reject id field or clearly document that id is ignored.

### Bug 8
- **Executive Summary:** PUT /todos/:id with only title resets fields
- **Description of Bug:** PUT with only 	itle resets doneStatus to false and description to empty.
- **Potential Impact on System:** Data loss if clients expect partial updates.
- **Steps to Reproduce:**
  1. Create a todo with doneStatus=true and a non-empty description.
  2. PUT /todos/{id} with body { "title": "Updated" }.
- **Actual Result:** doneStatus=false, description="".
- **Expected Result (per documentation):** Unspecified fields should be preserved, or behavior documented clearly.
