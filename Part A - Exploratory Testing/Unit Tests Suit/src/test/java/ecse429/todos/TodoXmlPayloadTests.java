package ecse429.todos;

import com.fasterxml.jackson.databind.JsonNode;
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
 * XML payload support tests.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoXmlPayloadTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // POST /todos accepts XML payloads and creates a todo.
    void postTodosWithXmlReturns201() {
        String title = "XML Todo Create";
        String xml = "<todo>" +
                "<title>" + title + "</title>" +
                "<doneStatus>false</doneStatus>" +
                "<description>Created via XML</description>" +
                "</todo>";

        Response response = TestSupport.postXml("/todos", xml);
        assertEquals(201, response.statusCode);

        String id = findTodoIdByTitle(title);
        assertNotNull(id, "Created todo should be retrievable by title");
        TestSupport.delete("/todos/" + id);
    }

    @Test
    // PUT /todos/:id accepts XML payloads and updates a todo.
    void putTodoByIdWithXmlReturns200() {
        String id = TestSupport.createTodo("XML Update", false, "before");
        try {
            String xml = "<todo>" +
                    "<title>XML Updated</title>" +
                    "<doneStatus>true</doneStatus>" +
                    "<description>Updated via XML</description>" +
                    "</todo>";

            Response response = TestSupport.putXml("/todos/" + id, xml);
            assertEquals(200, response.statusCode);

            Response getResponse = TestSupport.get("/todos/" + id);
            assertEquals(200, getResponse.statusCode);
            JsonNode body = TestSupport.parseJson(getResponse.body);
            JsonNode todo = body.path("todos").get(0);
            assertEquals("XML Updated", todo.path("title").asText());
            assertEquals("true", todo.path("doneStatus").asText());
        } finally {
            TestSupport.delete("/todos/" + id);
        }
    }

    private static String findTodoIdByTitle(String title) {
        Response response = TestSupport.get("/todos");
        if (response.statusCode != 200) {
            return null;
        }
        JsonNode body = TestSupport.parseJson(response.body);
        JsonNode todos = body.path("todos");
        if (!todos.isArray()) {
            return null;
        }
        for (JsonNode todo : todos) {
            if (title.equals(todo.path("title").asText())) {
                return todo.path("id").asText(null);
            }
        }
        return null;
    }
}
