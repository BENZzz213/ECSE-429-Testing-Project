package ecse429.todos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecse429.support.TestSupport;
import ecse429.support.TestSupport.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Undocumented / divergent behaviors discovered during exploratory testing.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoUndocumentedBehaviorTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // POST /todos/:id ignores an id field in the body and keeps the original id.
    void postTodoByIdIgnoresIdField() {
        String id = TestSupport.createTodo("Undoc Id Field", false, "before");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", 100);
            Response response = TestSupport.postJson("/todos/" + id, TestSupport.toJson(payload));
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertEquals(id, body.path("id").asText(), "Todo id should remain unchanged");
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // PUT /todos/:id with only title resets doneStatus to false and description to empty.
    void putTodoByIdWithOnlyTitleResetsDefaults() {
        String id = TestSupport.createTodo("Before Reset", true, "has description");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("title", "Updated Task (PUT)");

            Response response = TestSupport.putJson("/todos/" + id, TestSupport.toJson(payload));
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertEquals("false", body.path("doneStatus").asText());
            assertEquals("", body.path("description").asText());
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // POST /todos/:id allows partial updates without mandatory fields.
    void postTodoByIdAllowsPartialWithoutTitle() {
        String id = TestSupport.createTodo("Partial Update", false, "before");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("description", "Amended via POST");

            Response response = TestSupport.postJson("/todos/" + id, TestSupport.toJson(payload));
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertEquals("Amended via POST", body.path("description").asText());
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // POST /todos/:id/categories returns 404 when the todo does not exist (Swagger says 400 on errors).
    void postTodoCategoriesMissingTodoReturns404() {
        String categoryId = TestSupport.createCategory("Missing Todo", "for 404");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", categoryId);

            Response response = TestSupport.postJson("/todos/99999999/categories",
                    TestSupport.toJson(payload));
            assertEquals(404, response.statusCode);
        } finally {
            TestSupport.delete("/categories/" + categoryId);
        }
    }

    @Test
    // POST /todos/:id/categories with a non-existent category returns 404 (undocumented).
    void postTodoCategoriesMissingCategoryReturns404() {
        String todoId = TestSupport.createTodo("Missing Category", false, "for 400");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", "99999999");

            Response response = TestSupport.postJson("/todos/" + todoId + "/categories",
                    TestSupport.toJson(payload));
            assertEquals(404, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
        }
    }

    @Test
    // GET /todos/:id/categories/:id returns 404 (undocumented; Swagger says 405).
    void getTodoCategoryRelationshipReturns404() {
        Response response = TestSupport.get("/todos/1/categories/1");
        assertEquals(404, response.statusCode);
    }

    @Test
    // POST /todos/:id/categories/:id returns 404 (undocumented; Swagger says 405).
    void postTodoCategoryRelationshipReturns404() {
        Response response = TestSupport.postJson("/todos/1/categories/1", "{}");
        assertEquals(404, response.statusCode);
    }

    @Test
    // HEAD /todos/:id/categories/:id returns 404 (undocumented; Swagger says 405).
    void headTodoCategoryRelationshipReturns404() {
        Response response = TestSupport.head("/todos/1/categories/1");
        assertEquals(404, response.statusCode);
    }

    @Test
    // POST /todos/:id/tasksof returns 404 when the todo does not exist (undocumented; Swagger says 400).
    void postTodoTasksofMissingTodoReturns404() {
        String projectId = TestSupport.createProject("Missing Todo", false, false, "for 404");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", projectId);

            Response response = TestSupport.postJson("/todos/99999999/tasksof",
                    TestSupport.toJson(payload));
            assertEquals(404, response.statusCode);
        } finally {
            TestSupport.delete("/projects/" + projectId);
        }
    }

    @Test
    // POST /todos/:id/tasksof returns 404 when the project does not exist (undocumented; Swagger says 400).
    void postTodoTasksofMissingProjectReturns404() {
        String todoId = TestSupport.createTodo("Missing Project", false, "for 404");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", "99999999");

            Response response = TestSupport.postJson("/todos/" + todoId + "/tasksof",
                    TestSupport.toJson(payload));
            assertEquals(404, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
        }
    }

    @Test
    // GET /todos/:id/tasksof/:id returns 404 (undocumented; Swagger says 405).
    void getTodoTasksofRelationshipReturns404() {
        Response response = TestSupport.get("/todos/1/tasksof/1");
        assertEquals(404, response.statusCode);
    }

    @Test
    // POST /todos/:id/tasksof/:id returns 404 (undocumented; Swagger says 405).
    void postTodoTasksofRelationshipReturns404() {
        Response response = TestSupport.postJson("/todos/1/tasksof/1", "{}");
        assertEquals(404, response.statusCode);
    }

    @Test
    // HEAD /todos/:id/tasksof/:id returns 404 (undocumented; Swagger says 405).
    void headTodoTasksofRelationshipReturns404() {
        Response response = TestSupport.head("/todos/1/tasksof/1");
        assertEquals(404, response.statusCode);
    }

}
