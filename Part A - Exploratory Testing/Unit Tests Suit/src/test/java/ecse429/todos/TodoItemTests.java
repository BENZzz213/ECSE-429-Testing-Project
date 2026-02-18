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
 * /todos/:id item endpoints.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoItemTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // GET /todos/:id returns 200 and the matching todo.
    void getTodoByIdReturns200() {
        String id = TestSupport.createTodo("Get Todo By Id", false, "get test");
        try {
            Response response = TestSupport.get("/todos/" + id);
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertEquals(id, body.path("todos").get(0).path("id").asText());
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // GET /todos/:id returns 404 for a non-existent id.
    void getTodoByIdReturns404WhenMissing() {
        Response response = TestSupport.get("/todos/99999999");
        assertEquals(404, response.statusCode);
    }

    @Test
    // PUT /todos/:id updates fields and returns 200.
    void putTodoByIdUpdatesAndReturns200() {
        String id = TestSupport.createTodo("Put Todo", false, "before");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("title", "Updated Task (PUT)");
            payload.put("doneStatus", true);
            payload.put("description", "Updated via PUT");

            Response response = TestSupport.putJson("/todos/" + id, TestSupport.toJson(payload));
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertEquals("Updated Task (PUT)", body.path("title").asText());
            assertEquals("true", body.path("doneStatus").asText());
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // PUT /todos/:id without title returns 400.
    void putTodoByIdMissingTitleReturns400() {
        String id = TestSupport.createTodo("Put Missing Title", false, "before");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("description", "no title");
            Response response = TestSupport.putJson("/todos/" + id, TestSupport.toJson(payload));
            assertEquals(400, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // PUT /todos/:id with empty title returns 400.
    void putTodoByIdEmptyTitleReturns400() {
        String id = TestSupport.createTodo("Put Empty Title", false, "before");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("title", "");
            Response response = TestSupport.putJson("/todos/" + id, TestSupport.toJson(payload));
            assertEquals(400, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // PUT /todos/:id returns 404 for a non-existent id.
    void putTodoByIdReturns404WhenMissing() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("title", "Does not matter");
        payload.put("doneStatus", false);
        payload.put("description", "missing id");

        Response response = TestSupport.putJson("/todos/99999999", TestSupport.toJson(payload));
        assertEquals(404, response.statusCode);
    }

    @Test
    // POST /todos/:id amends fields and returns 200.
    void postTodoByIdAmendsAndReturns200() {
        String id = TestSupport.createTodo("Post Todo", false, "before");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("title", "Amended via POST");
            payload.put("description", "Amended via POST");

            Response response = TestSupport.postJson("/todos/" + id, TestSupport.toJson(payload));
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertEquals("Amended via POST", body.path("title").asText());
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // POST /todos/:id returns 404 for a non-existent id.
    void postTodoByIdReturns404WhenMissing() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("description", "missing id");

        Response response = TestSupport.postJson("/todos/99999999", TestSupport.toJson(payload));
        assertEquals(404, response.statusCode);
    }

    @Test
    // DELETE /todos/:id returns 200 and removes the todo.
    void deleteTodoByIdReturns200() {
        String id = TestSupport.createTodo("Delete Todo", false, "to delete");
        Response response = TestSupport.delete("/todos/" + id);
        assertEquals(200, response.statusCode);

        Response getResponse = TestSupport.get("/todos/" + id);
        assertEquals(404, getResponse.statusCode);
    }

    @Test
    // DELETE /todos/:id twice returns 404 on the second attempt.
    void deleteTodoByIdTwiceReturns404() {
        String id = TestSupport.createTodo("Delete Twice", false, "to delete twice");
        Response first = TestSupport.delete("/todos/" + id);
        assertEquals(200, first.statusCode);

        Response second = TestSupport.delete("/todos/" + id);
        assertEquals(404, second.statusCode);
    }

    @Test
    // HEAD /todos/:id returns 200 and no body for an existing todo.
    void headTodoByIdReturns200() {
        String id = TestSupport.createTodo("Head Todo", false, "head test");
        try {
            Response response = TestSupport.head("/todos/" + id);
            assertEquals(200, response.statusCode);
            assertTrue(response.body == null || response.body.isEmpty(), "HEAD should not return a body");
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // OPTIONS /todos/:id returns 200 and lists allowed methods.
    void optionsTodoByIdReturns200AndAllowHeader() {
        String id = TestSupport.createTodo("Options Todo", false, "options test");
        try {
            Response response = TestSupport.options("/todos/" + id);
            assertEquals(200, response.statusCode);

            String allow = response.header("Allow");
            assertNotNull(allow, "Allow header should be present");
            String allowUpper = allow.toUpperCase();
            assertTrue(allowUpper.contains("OPTIONS"));
            assertTrue(allowUpper.contains("GET"));
            assertTrue(allowUpper.contains("HEAD"));
            assertTrue(allowUpper.contains("POST"));
            assertTrue(allowUpper.contains("PUT"));
            assertTrue(allowUpper.contains("DELETE"));
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    @Test
    // PATCH /todos/:id returns 405 (method not allowed).
    void patchTodoByIdReturns405() {
        Response response = TestSupport.patchJson("/todos/1", "{}");
        assertEquals(405, response.statusCode);
    }

    
}
