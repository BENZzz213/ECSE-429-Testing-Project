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
 * /todos/:id/categories/:id relationship endpoints.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoCategoryRelationshipTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // DELETE /todos/:id/categories/:id removes the relationship and returns 200.
    void deleteTodoCategoryRelationshipReturns200() {
        String todoId = TestSupport.createTodo("Todo to Link", false, "for delete");
        String categoryId = TestSupport.createCategory("Office", "for delete");
        linkCategory(todoId, categoryId);
        try {
            Response response = TestSupport.delete("/todos/" + todoId + "/categories/" + categoryId);
            assertEquals(200, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/categories/" + categoryId);
        }
    }

    @Test
    // DELETE /todos/:id/categories/:id returns 404 when relationship does not exist.
    void deleteTodoCategoryRelationshipReturns404WhenMissing() {
        String todoId = TestSupport.createTodo("Todo Missing Link", false, "for 404");
        String categoryId = TestSupport.createCategory("Office", "for 404");
        try {
            Response response = TestSupport.delete("/todos/" + todoId + "/categories/" + categoryId);
            assertEquals(404, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/categories/" + categoryId);
        }
    }

    @Test
    // OPTIONS /todos/:id/categories/:id returns 200.
    void optionsTodoCategoryRelationshipReturns200() {
        Response response = TestSupport.options("/todos/1/categories/1");
        assertEquals(200, response.statusCode);
    }

    @Test
    // PUT /todos/:id/categories/:id returns 405 (documented behavior).
    void putTodoCategoryRelationshipReturns405() {
        Response response = TestSupport.putJson("/todos/1/categories/1", "{}");
        assertEquals(405, response.statusCode);
    }

    @Test
    // PATCH /todos/:id/categories/:id returns 405 (documented behavior).
    void patchTodoCategoryRelationshipReturns405() {
        Response response = TestSupport.patchJson("/todos/1/categories/1", "{}");
        assertEquals(405, response.statusCode);
    }

    private static void linkCategory(String todoId, String categoryId) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("id", categoryId);
        Response response = TestSupport.postJson("/todos/" + todoId + "/categories",
                TestSupport.toJson(payload));
        assertTrue(response.statusCode == 200 || response.statusCode == 201);
    }
}
