package ecse429.todos;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ecse429.support.TestSupport;
import ecse429.support.TestSupport.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * /todos/:id/tasksof/:id relationship delete.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoTasksofRelationshipTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // DELETE /todos/:id/tasksof/:id removes the relationship and returns 200.
    void deleteTodoTasksofRelationshipReturns200() {
        String todoId = TestSupport.createTodo("Todo to Link", false, "for delete");
        String projectId = TestSupport.createProject("Office Work", false, false, "for delete");
        linkProject(todoId, projectId);
        try {
            Response response = TestSupport.delete("/todos/" + todoId + "/tasksof/" + projectId);
            assertEquals(200, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/projects/" + projectId);
        }
    }

    @Test
    // DELETE /todos/:id/tasksof/:id returns 404 when relationship does not exist.
    void deleteTodoTasksofRelationshipReturns404WhenMissing() {
        String todoId = TestSupport.createTodo("Todo Missing Link", false, "for 404");
        String projectId = TestSupport.createProject("Office Work", false, false, "for 404");
        try {
            Response response = TestSupport.delete("/todos/" + todoId + "/tasksof/" + projectId);
            assertEquals(404, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/projects/" + projectId);
        }
    }

    @Test
    // OPTIONS /todos/:id/tasksof/:id returns 200.
    void optionsTodoTasksofRelationshipReturns200() {
        Response response = TestSupport.options("/todos/1/tasksof/1");
        assertEquals(200, response.statusCode);
    }

    @Test
    // PUT /todos/:id/tasksof/:id returns 405 (documented behavior).
    void putTodoTasksofRelationshipReturns405() {
        Response response = TestSupport.putJson("/todos/1/tasksof/1", "{}");
        assertEquals(405, response.statusCode);
    }

    @Test
    // PATCH /todos/:id/tasksof/:id returns 405 (documented behavior).
    void patchTodoTasksofRelationshipReturns405() {
        Response response = TestSupport.patchJson("/todos/1/tasksof/1", "{}");
        assertEquals(405, response.statusCode);
    }

    private static void linkProject(String todoId, String projectId) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("id", projectId);
        Response response = TestSupport.postJson("/todos/" + todoId + "/tasksof",
                TestSupport.toJson(payload));
        assertTrue(response.statusCode == 200 || response.statusCode == 201);
    }
}
