package ecse429.bdd.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ecse429.bdd.support.TestSupport;
import ecse429.bdd.support.TestSupport.Response;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TodoStepDefinitions {
    private final Set<String> todoIdsToCleanup = new LinkedHashSet<>();
    private final Set<String> categoryIdsToCleanup = new LinkedHashSet<>();

    private Response lastResponse;
    private String currentTodoId;
    private String currentCategoryId;
    private String currentTodoTitle;
    private String currentTodoDescription;

    @Before
    public void beforeScenario() {
        lastResponse = null;
        currentTodoId = null;
        currentCategoryId = null;
        currentTodoTitle = null;
        currentTodoDescription = null;
        todoIdsToCleanup.clear();
        categoryIdsToCleanup.clear();
    }

    @After
    public void afterScenario() {
        for (String todoId : todoIdsToCleanup) {
            TestSupport.deleteIfPresent("/todos/" + todoId);
        }
        for (String categoryId : categoryIdsToCleanup) {
            TestSupport.deleteIfPresent("/categories/" + categoryId);
        }
    }

    @Given("the Todo Manager API is running")
    public void theTodoManagerApiIsRunning() {
        TestSupport.requireServiceUp();
    }

    @Given("a todo exists with title {string}, description {string}, and done status {string}")
    public void aTodoExistsWithTitleDescriptionAndDoneStatus(String title, String description, String doneStatus) {
        boolean done = parseDoneStatus(doneStatus);
        currentTodoId = TestSupport.createTodo(title, done, description);
        todoIdsToCleanup.add(currentTodoId);
        currentTodoTitle = title;
        currentTodoDescription = description;
    }

    @Given("a category exists with title {string} and description {string}")
    public void aCategoryExistsWithTitleAndDescription(String title, String description) {
        currentCategoryId = TestSupport.createCategory(title, description);
        categoryIdsToCleanup.add(currentCategoryId);
    }

    @When("I create a todo with title {string}")
    public void iCreateATodoWithTitle(String title) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("title", title);

        lastResponse = TestSupport.postJson("/todos", TestSupport.toJson(payload));
        if (lastResponse.statusCode == 201) {
            rememberTodoFromCreateResponse(title, "");
        }
    }

    @When("I create a todo with title {string}, description {string}, and done status {string}")
    public void iCreateATodoWithTitleDescriptionAndDoneStatus(String title, String description, String doneStatus) {
        boolean done = parseDoneStatus(doneStatus);
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("doneStatus", done);

        lastResponse = TestSupport.postJson("/todos", TestSupport.toJson(payload));
        if (lastResponse.statusCode == 201) {
            rememberTodoFromCreateResponse(title, description);
        }
    }

    @When("I create a todo without a title and with description {string}")
    public void iCreateATodoWithoutATitleAndWithDescription(String description) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("description", description);

        lastResponse = TestSupport.postJson("/todos", TestSupport.toJson(payload));
    }

    @When("I retrieve the created todo by id")
    public void iRetrieveTheCreatedTodoById() {
        lastResponse = TestSupport.get("/todos/" + requireTodoId());
    }

    @When("I retrieve the todo by id")
    public void iRetrieveTheTodoById() {
        lastResponse = TestSupport.get("/todos/" + requireTodoId());
    }

    @When("I replace the todo with title {string}, description {string}, and done status {string}")
    public void iReplaceTheTodoWithTitleDescriptionAndDoneStatus(String title, String description, String doneStatus) {
        boolean done = parseDoneStatus(doneStatus);
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("doneStatus", done);

        lastResponse = TestSupport.putJson("/todos/" + requireTodoId(), TestSupport.toJson(payload));
        if (lastResponse.statusCode == 200) {
            currentTodoTitle = title;
            currentTodoDescription = description;
        }
    }

    @When("I partially update only the todo description to {string}")
    public void iPartiallyUpdateOnlyTheTodoDescriptionTo(String description) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("description", description);

        lastResponse = TestSupport.postJson("/todos/" + requireTodoId(), TestSupport.toJson(payload));
        if (lastResponse.statusCode == 200) {
            currentTodoDescription = description;
        }
    }

    @When("I try to replace the todo with an empty title")
    public void iTryToReplaceTheTodoWithAnEmptyTitle() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("title", "");

        lastResponse = TestSupport.putJson("/todos/" + requireTodoId(), TestSupport.toJson(payload));
    }

    @When("I mark the todo as done")
    public void iMarkTheTodoAsDone() {
        updateTodoStatus(true);
    }

    @When("I mark the todo as not done")
    public void iMarkTheTodoAsNotDone() {
        updateTodoStatus(false);
    }

    @When("I delete the todo")
    public void iDeleteTheTodo() {
        String todoId = requireTodoId();
        lastResponse = TestSupport.delete("/todos/" + todoId);
        if (lastResponse.statusCode == 200) {
            todoIdsToCleanup.remove(todoId);
        }
    }

    @When("I delete the created todo")
    public void iDeleteTheCreatedTodo() {
        iDeleteTheTodo();
    }

    @When("I delete the same todo again")
    public void iDeleteTheSameTodoAgain() {
        lastResponse = TestSupport.delete("/todos/" + requireTodoId());
    }

    @When("I link the todo to the category")
    public void iLinkTheTodoToTheCategory() {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("id", requireCategoryId());
        lastResponse = TestSupport.postJson("/todos/" + requireTodoId() + "/categories", TestSupport.toJson(payload));
    }

    @When("I retrieve the categories for the todo")
    public void iRetrieveTheCategoriesForTheTodo() {
        lastResponse = TestSupport.get("/todos/" + requireTodoId() + "/categories");
    }

    @When("I try to link the todo to the non-existent category with id {string}")
    public void iTryToLinkTheTodoToTheNonExistentCategoryWithId(String categoryId) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("id", categoryId);
        lastResponse = TestSupport.postJson("/todos/" + requireTodoId() + "/categories", TestSupport.toJson(payload));
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertNotNull(lastResponse, "No response is available for assertion");
        assertEquals(expectedStatus, lastResponse.statusCode);
    }

    @Then("^the (created|updated|retrieved) todo should have title \"([^\"]*)\"$")
    public void theScopedTodoShouldHaveTitle(String scope, String expectedTitle) {
        JsonNode todo = extractTodo(scope);
        assertEquals(expectedTitle, todo.path("title").asText());
    }

    @Then("^the (created|updated|retrieved) todo should have description \"([^\"]*)\"$")
    public void theScopedTodoShouldHaveDescription(String scope, String expectedDescription) {
        JsonNode todo = extractTodo(scope);
        assertEquals(expectedDescription, todo.path("description").asText());
    }

    @Then("the created todo should have an empty description")
    public void theCreatedTodoShouldHaveAnEmptyDescription() {
        JsonNode todo = extractTodo("created");
        assertEquals("", todo.path("description").asText());
    }

    @Then("^the (created|updated|retrieved) todo should be marked as (done|not done)$")
    public void theScopedTodoShouldBeMarkedAs(String scope, String expectedStatus) {
        JsonNode todo = extractTodo(scope);
        boolean expectedDone = "done".equals(expectedStatus);
        assertEquals(expectedDone, asBoolean(todo.path("doneStatus")));
    }

    @Then("the response should not include a todo id")
    public void theResponseShouldNotIncludeATodoId() {
        assertNotNull(lastResponse, "No response is available for assertion");
        JsonNode body = TestSupport.parseJson(lastResponse.body);
        String id = body.path("id").asText("");
        assertTrue(id.isBlank(), "Error response should not include a created todo id");
    }

    @Then("the error message should mention {string}")
    public void theErrorMessageShouldMention(String text) {
        assertNotNull(lastResponse, "No response is available for assertion");
        assertTrue(lastResponse.body.toLowerCase().contains(text.toLowerCase()),
                "Expected error body to mention '" + text + "', but was: " + lastResponse.body);
    }

    @Then("retrieving the deleted todo by id should return status {int}")
    public void retrievingTheDeletedTodoByIdShouldReturnStatus(int expectedStatus) {
        Response response = TestSupport.get("/todos/" + requireTodoId());
        assertEquals(expectedStatus, response.statusCode);
    }

    @Then("the retrieved categories should include the linked category")
    public void theRetrievedCategoriesShouldIncludeTheLinkedCategory() {
        JsonNode categories = extractCategories();
        assertTrue(arrayContainsId(categories, requireCategoryId()),
                "Expected categories to include linked category id " + currentCategoryId);
    }

    @Then("the retrieved categories should contain title {string}")
    public void theRetrievedCategoriesShouldContainTitle(String expectedTitle) {
        JsonNode categories = extractCategories();
        assertTrue(arrayContainsField(categories, "title", expectedTitle),
                "Expected categories to include title '" + expectedTitle + "'");
    }

    @Then("the retrieved categories should contain description {string}")
    public void theRetrievedCategoriesShouldContainDescription(String expectedDescription) {
        JsonNode categories = extractCategories();
        assertTrue(arrayContainsField(categories, "description", expectedDescription),
                "Expected categories to include description '" + expectedDescription + "'");
    }

    @Then("the retrieved categories should be empty")
    public void theRetrievedCategoriesShouldBeEmpty() {
        JsonNode categories = extractCategories();
        assertTrue(categories.isArray(), "Expected 'categories' to be an array");
        assertEquals(0, categories.size(), "Expected no linked categories");
    }

    private void rememberTodoFromCreateResponse(String title, String description) {
        JsonNode body = TestSupport.parseJson(lastResponse.body);
        currentTodoId = body.path("id").asText(null);
        assertNotNull(currentTodoId, "Created todo should include an id");
        todoIdsToCleanup.add(currentTodoId);
        currentTodoTitle = title;
        currentTodoDescription = description;
    }

    private void updateTodoStatus(boolean doneStatus) {
        ObjectNode payload = TestSupport.jsonObject();
        payload.put("title", currentTodoTitle);
        payload.put("description", currentTodoDescription);
        payload.put("doneStatus", doneStatus);

        lastResponse = TestSupport.putJson("/todos/" + requireTodoId(), TestSupport.toJson(payload));
    }

    private JsonNode extractTodo(String scope) {
        assertNotNull(lastResponse, "No response is available for todo assertion");
        JsonNode body = TestSupport.parseJson(lastResponse.body);
        if ("retrieved".equals(scope)) {
            JsonNode todos = body.path("todos");
            assertTrue(todos.isArray() && todos.size() > 0, "Expected 'todos' array in retrieve response");
            return todos.get(0);
        }
        return body;
    }

    private JsonNode extractCategories() {
        assertNotNull(lastResponse, "No response is available for categories assertion");
        JsonNode body = TestSupport.parseJson(lastResponse.body);
        JsonNode categories = body.path("categories");
        assertTrue(categories.isArray(), "Expected 'categories' to be an array");
        return categories;
    }

    private static boolean parseDoneStatus(String doneStatus) {
        if ("true".equalsIgnoreCase(doneStatus)) {
            return true;
        }
        if ("false".equalsIgnoreCase(doneStatus)) {
            return false;
        }
        throw new AssertionError("Unsupported done status value: " + doneStatus);
    }

    private static boolean asBoolean(JsonNode node) {
        return "true".equalsIgnoreCase(node.asText());
    }

    private static boolean arrayContainsId(JsonNode arrayNode, String id) {
        for (JsonNode item : arrayNode) {
            if (id.equals(item.path("id").asText())) {
                return true;
            }
        }
        return false;
    }

    private static boolean arrayContainsField(JsonNode arrayNode, String fieldName, String expectedValue) {
        for (JsonNode item : arrayNode) {
            if (expectedValue.equals(item.path(fieldName).asText())) {
                return true;
            }
        }
        return false;
    }

    private String requireTodoId() {
        assertNotNull(currentTodoId, "No todo id is available in the current scenario");
        return currentTodoId;
    }

    private String requireCategoryId() {
        assertNotNull(currentCategoryId, "No category id is available in the current scenario");
        return currentCategoryId;
    }
}
