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
 * /todos/:id/categories relationship collection endpoints.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class TodoCategoriesCollectionTests {

    @BeforeAll
    static void checkServiceIsUp() {
        TestSupport.requireServiceUp();
    }

    @Test
    // GET /todos/:id/categories returns 200 and a categories array for a valid todo.
    void getTodoCategoriesReturns200AndArray() {
        String todoId = TestSupport.createTodo("Todo with Category", false, "for categories");
        String categoryId = TestSupport.createCategory("Office", "for categories");
        try {
            Response link = TestSupport.postJson("/todos/" + todoId + "/categories",
                    TestSupport.toJson(TestSupport.jsonObject().put("id", categoryId)));
            assertEquals(201, link.statusCode);

            Response response = TestSupport.get("/todos/" + todoId + "/categories");
            assertEquals(200, response.statusCode);

            JsonNode body = TestSupport.parseJson(response.body);
            assertTrue(body.has("categories"), "Response should include 'categories' field");
            assertTrue(body.get("categories").isArray(), "'categories' should be an array");
            assertTrue(containsId(body.get("categories"), categoryId),
                    "Categories list should include linked category id");
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/categories/" + categoryId);
        }
    }

    @Test
    // POST /todos/:id/categories returns 201 when creating a relationship (documented behavior).
    void postTodoCategoriesReturns201() {
        String todoId = TestSupport.createTodo("Todo to Link", false, "for post");
        String categoryId = TestSupport.createCategory("Office", "for post");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", categoryId);

            Response response = TestSupport.postJson("/todos/" + todoId + "/categories",
                    TestSupport.toJson(payload));
            assertEquals(201, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
            TestSupport.delete("/categories/" + categoryId);
        }
    }

    @Test
    @Disabled("Documented 400; actual behavior returns 404 (see TodoUndocumentedBehaviorTests).")
    // POST /todos/:id/categories with a non-existent todo should return 400 (documented).
    void postTodoCategoriesMissingTodoReturns400_Documented() {
        String categoryId = TestSupport.createCategory("Missing Todo", "for 400");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", categoryId);

            Response response = TestSupport.postJson("/todos/99999999/categories",
                    TestSupport.toJson(payload));
            assertEquals(400, response.statusCode);
        } finally {
            TestSupport.delete("/categories/" + categoryId);
        }
    }

    @Test
    @Disabled("Documented 400; actual behavior returns 404 (see TodoUndocumentedBehaviorTests).")
    // POST /todos/:id/categories with a non-existent category should return 400 (documented).
    void postTodoCategoriesMissingCategoryReturns400_Documented() {
        String todoId = TestSupport.createTodo("Missing Category", false, "for 400");
        try {
            ObjectNode payload = TestSupport.jsonObject();
            payload.put("id", "99999999");

            Response response = TestSupport.postJson("/todos/" + todoId + "/categories",
                    TestSupport.toJson(payload));
            assertEquals(400, response.statusCode);
        } finally {
            TestSupport.delete("/todos/" + todoId);
        }
    }

    @Test
    // HEAD /todos/:id/categories returns 200 with no body.
    void headTodoCategoriesReturns200() {
        String todoId = TestSupport.createTodo("Head Categories", false, "head");
        try {
            Response response = TestSupport.head("/todos/" + todoId + "/categories");
            assertEquals(200, response.statusCode);
            assertTrue(response.body == null || response.body.isEmpty(), "HEAD should not return a body");
        } finally {
            TestSupport.delete("/todos/" + todoId);
        }
    }

    @Test
    // OPTIONS /todos/:id/categories returns 200 and lists allowed methods.
    void optionsTodoCategoriesReturns200AndAllowHeader() {
        String todoId = TestSupport.createTodo("Options Categories", false, "options");
        try {
            Response response = TestSupport.options("/todos/" + todoId + "/categories");
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
    // PUT /todos/:id/categories returns 405 (method not allowed).
    void putTodoCategoriesReturns405() {
        Response response = TestSupport.request("PUT", "/todos/1/categories", null, null);
        assertEquals(405, response.statusCode);
    }

    @Test
    // DELETE /todos/:id/categories returns 405 (method not allowed).
    void deleteTodoCategoriesReturns405() {
        Response response = TestSupport.delete("/todos/1/categories");
        assertEquals(405, response.statusCode);
    }

    @Test
    // PATCH /todos/:id/categories returns 405 (method not allowed).
    void patchTodoCategoriesReturns405() {
        Response response = TestSupport.patchJson("/todos/1/categories", "{}");
        assertEquals(405, response.statusCode);
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
