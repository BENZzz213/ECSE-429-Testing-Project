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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * /todos collection endpoints.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodosCollectionTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // GET /todos returns 200 and a JSON array of todos.
    void getTodosReturns200AndArray() {
        Response response = TestSupport.get("/todos");
        assertEquals(200, response.statusCode);

        JsonNode body = TestSupport.parseJson(response.body);
        assertTrue(body.has("todos"), "Response should include 'todos' field");
        assertTrue(body.get("todos").isArray(), "'todos' should be an array");
    }

    @Test
    // POST /todos creates a todo and returns 201 with the created entity.
    void postTodosCreatesTodoAndReturns201() {
        String id = null;
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("title", "Exploratory Task");
            payload.put("doneStatus", false);
            payload.put("description", "Testing CRUD");

            Response response = TestSupport.postJson("/todos", TestSupport.toJson(payload));
            assertEquals(201, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            id = body.path("id").asText(null);
            assertNotNull(id, "Created todo should include an id");
            assertEquals("Exploratory Task", body.path("title").asText());
        } finally {
            // Delete the todo we just created
            if (id != null) {
                TestSupport.delete("/todos/" + id);
            }
        }
    }

    @Test
    // POST /todos without title returns 400.
    void postTodosMissingTitleReturns400() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("doneStatus", false);
        payload.put("description", "No title");

        Response response = TestSupport.postJson("/todos", TestSupport.toJson(payload));
        assertEquals(400, response.statusCode);
        assertTrue(response.body.contains("title"), "Error message should mention title");
    }

    @Test
    // POST /todos with invalid doneStatus type returns 400.
    void postTodosInvalidDoneStatusTypeReturns400() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("title", "Invalid doneStatus");
        payload.put("doneStatus", 1);
        payload.put("description", "Bad type");

        Response response = TestSupport.postJson("/todos", TestSupport.toJson(payload));
        assertEquals(400, response.statusCode);
        assertTrue(response.body.toLowerCase().contains("donestatus"), "Error message should mention doneStatus");
    }

    @Test
    // POST /todos with an id field returns 400.
    void postTodosWithIdReturns400() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("id", 100);
        payload.put("title", "Should fail");
        payload.put("doneStatus", false);
        payload.put("description", "Has id");

        Response response = TestSupport.postJson("/todos", TestSupport.toJson(payload));
        assertEquals(400, response.statusCode);
        assertTrue(response.body.toLowerCase().contains("id"), "Error message should mention id");
    }

    @Test
    // HEAD /todos returns 200 and a JSON content-type header with no body.
    void headTodosReturns200AndContentTypeHeader() {
        Response response = TestSupport.head("/todos");
        assertEquals(200, response.statusCode);
        assertTrue(response.body == null || response.body.isEmpty(), "HEAD should not return a body");

        String contentType = response.header("Content-Type");
        assertNotNull(contentType, "Content-Type header should be present");
        assertTrue(contentType.toLowerCase().contains("application/json"),
                "Content-Type should be application/json");
    }

    @Test
    // OPTIONS /todos returns 200 and lists allowed methods in the Allow header.
    void optionsTodosReturns200AndAllowHeader() {
        Response response = TestSupport.options("/todos");
        assertEquals(200, response.statusCode);

        String allow = response.header("Allow");
        assertNotNull(allow, "Allow header should be present");
        String allowUpper = allow.toUpperCase();
        assertTrue(allowUpper.contains("OPTIONS"));
        assertTrue(allowUpper.contains("GET"));
        assertTrue(allowUpper.contains("HEAD"));
        assertTrue(allowUpper.contains("POST"));
    }

    @Test
    // PUT /todos returns 405 (method not allowed).
    void putTodosReturns405() {
        Response response = TestSupport.request("PUT", "/todos", null, null);
        assertEquals(405, response.statusCode);
    }

    @Test
    // DELETE /todos returns 405 (method not allowed).
    void deleteTodosReturns405() {
        Response response = TestSupport.delete("/todos");
        assertEquals(405, response.statusCode);
    }

    @Test
    // PATCH /todos returns 405 (method not allowed).
    void patchTodosReturns405() {
        Response response = TestSupport.patchJson("/todos", "{}");
        assertEquals(405, response.statusCode);
    }
}
