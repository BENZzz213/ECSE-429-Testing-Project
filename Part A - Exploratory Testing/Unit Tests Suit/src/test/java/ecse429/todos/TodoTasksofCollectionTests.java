package ecse429.todos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecse429.support.TestSupport;
import ecse429.support.TestSupport.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * /todos/:id/tasksof relationship collection.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoTasksofCollectionTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // GET /todos/:id/tasksof returns 200 and a projects array for a valid todo.
    void getTodoTasksofReturns200AndArray() {
        String todoId = TestSupport.createTodo("Todo with Project", false, "for tasksof");
        String projectId = TestSupport.createProject("Office Work", false, false, "for tasksof");
        try {
            linkProject(todoId, projectId);

            Response response = TestSupport.get("/todos/" + todoId + "/tasksof");
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertTrue(body.has("projects"), "Response should include 'projects' field");
            assertTrue(body.get("projects").isArray(), "'projects' should be an array");
            assertTrue(containsId(body.get("projects"), projectId),
                    "Projects list should include linked project id");
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/projects/" + projectId);
        }
    }

    @Test
    // POST /todos/:id/tasksof returns 201 when creating a relationship (documented behavior).
    void postTodoTasksofReturns201() {
        String todoId = TestSupport.createTodo("Todo to Link", false, "for post");
        String projectId = TestSupport.createProject("Office Work", false, false, "for post");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", projectId);

            Response response = TestSupport.postJson("/todos/" + todoId + "/tasksof",
                    TestSupport.toJson(payload));
            assertEquals(201, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/projects/" + projectId);
        }
    }

    @Test
    @Disabled("Documented 400; actual behavior returns 404 (see TodoUndocumentedBehaviorTests).")
    // POST /todos/:id/tasksof with a non-existent todo should return 400 (documented).
    void postTodoTasksofMissingTodoReturns400_Documented() {
        String projectId = TestSupport.createProject("Missing Todo", false, false, "for 400");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", projectId);

            Response response = TestSupport.postJson("/todos/99999999/tasksof",
                    TestSupport.toJson(payload));
            assertEquals(400, response.statusCode);
        } finally {
            TestSupport.delete("/projects/" + projectId);
        }
    }

    @Test
    @Disabled("Documented 400; actual behavior returns 404 (see TodoUndocumentedBehaviorTests).")
    // POST /todos/:id/tasksof with a non-existent project should return 400 (documented).
    void postTodoTasksofMissingProjectReturns400_Documented() {
        String todoId = TestSupport.createTodo("Missing Project", false, "for 400");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", "99999999");

            Response response = TestSupport.postJson("/todos/" + todoId + "/tasksof",
                    TestSupport.toJson(payload));
            assertEquals(400, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
        }
    }

    @Test
    // HEAD /todos/:id/tasksof returns 200 with no body.
    void headTodoTasksofReturns200() {
        String todoId = TestSupport.createTodo("Head Tasksof", false, "head");
        try {
            Response response = TestSupport.head("/todos/" + todoId + "/tasksof");
            assertEquals(200, response.statusCode);
            assertTrue(response.body == null || response.body.isEmpty(), "HEAD should not return a body");
        } finally {
            TestSupport.delete("/todos/" + todoId);
        }
    }

    @Test
    // OPTIONS /todos/:id/tasksof returns 200 and lists allowed methods.
    void optionsTodoTasksofReturns200AndAllowHeader() {
        String todoId = TestSupport.createTodo("Options Tasksof", false, "options");
        try {
            Response response = TestSupport.options("/todos/" + todoId + "/tasksof");
            assertEquals(200, response.statusCode);

            String allow = response.header("Allow");
            assertNotNull(allow, "Allow header should be present");
            String allowUpper = allow.toUpperCase();
            assertTrue(allowUpper.contains("OPTIONS"));
            assertTrue(allowUpper.contains("GET"));
            assertTrue(allowUpper.contains("HEAD"));
            assertTrue(allowUpper.contains("POST"));
        } finally {
            TestSupport.delete("/todos/" + todoId);
        }
    }

    @Test
    // PUT /todos/:id/tasksof returns 405 (method not allowed).
    void putTodoTasksofReturns405() {
        Response response = TestSupport.request("PUT", "/todos/1/tasksof", null, null);
        assertEquals(405, response.statusCode);
    }

    @Test
    // DELETE /todos/:id/tasksof returns 405 (method not allowed).
    void deleteTodoTasksofReturns405() {
        Response response = TestSupport.delete("/todos/1/tasksof");
        assertEquals(405, response.statusCode);
    }

    @Test
    // PATCH /todos/:id/tasksof returns 405 (method not allowed).
    void patchTodoTasksofReturns405() {
        Response response = TestSupport.patchJson("/todos/1/tasksof", "{}");
        assertEquals(405, response.statusCode);
    }

    private static void linkProject(String todoId, String projectId) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("id", projectId);
        Response response = TestSupport.postJson("/todos/" + todoId + "/tasksof",
                TestSupport.toJson(payload));
        assertTrue(response.statusCode == 200 || response.statusCode == 201);
    }

    private static boolean containsId(JsonNode arrayNode, String id) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return false;
        }
        for (JsonNode item : arrayNode) {
            if (id.equals(item.path("id").asText())) {
                return true;
            }
        }
        return false;
    }
}
