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
 * Cross-cutting negative cases required by the project.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoInvalidInputTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // POST /todos with malformed JSON returns 400 (Gson MalformedJsonException).
    void postTodosMalformedJsonReturns400() {
        String malformed = "{\n" +
                "  \"title\": \"cook dinner\",\n" +
                "  \"doneStatus\": false\n" +
                "  \"description\": \"pizza with salami\"\n" +
                "}";
        Response response = TestSupport.request("POST", "/todos", malformed, "application/json");
        assertEquals(400, response.statusCode);
        assertTrue(response.body.toLowerCase().contains("errormessages"), "Error response should mention errorMessages");
    }

    @Test
    // POST /todos with malformed XML returns 400.
    void postTodosMalformedXmlReturns400() {
        String malformed = "<todo><title>Bad</title>";
        Response response = TestSupport.request("POST", "/todos", malformed, "application/xml");
        assertEquals(400, response.statusCode);
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
    }

    @Test
    // POST /todos with id field returns 400.
    void postTodosWithIdReturns400() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("id", 100);
        payload.put("title", "Should fail");
        payload.put("doneStatus", false);
        payload.put("description", "Has id");

        Response response = TestSupport.postJson("/todos", TestSupport.toJson(payload));
        assertEquals(400, response.statusCode);
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

}
