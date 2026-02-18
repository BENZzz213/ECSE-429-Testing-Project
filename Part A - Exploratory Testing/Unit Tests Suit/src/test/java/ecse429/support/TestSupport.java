package ecse429.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Shared test support (HTTP client, state save/restore, random-order config).
 */
public final class TestSupport {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(TestConfig.CONNECT_TIMEOUT)
            .build();

    private TestSupport() {
        // utility class
    }

    public static Response get(String path) {
        return request("GET", path, null, null);
    }

    public static Response head(String path) {
        return request("HEAD", path, null, null);
    }

    public static Response options(String path) {
        return request("OPTIONS", path, null, null);
    }

    public static Response delete(String path) {
        return request("DELETE", path, null, null);
    }

    public static Response postJson(String path, String jsonBody) {
        return request("POST", path, jsonBody, "application/json");
    }

    public static Response putJson(String path, String jsonBody) {
        return request("PUT", path, jsonBody, "application/json");
    }

    public static Response postXml(String path, String xmlBody) {
        return request("POST", path, xmlBody, "application/xml");
    }

    public static Response putXml(String path, String xmlBody) {
        return request("PUT", path, xmlBody, "application/xml");
    }

    public static Response patchJson(String path, String jsonBody) {
        return request("PATCH", path, jsonBody, "application/json");
    }

    public static Response request(String method, String path, String body, String contentType) {
        String url = TestConfig.BASE_URL + path;
        HttpRequest.BodyPublisher publisher = (body == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TestConfig.REQUEST_TIMEOUT)
                .method(method, publisher)
                .header("Accept", "application/json, application/xml, */*");

        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }

        try {
            HttpResponse<String> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new Response(response.statusCode(), response.body(), response.headers().map());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("HTTP request failed: " + method + " " + url + " -> " + e.getMessage(), e);
        }
    }

    public static void requireServiceUp() {
        Response response = get("/todos");
        if (response.statusCode != 200) {
            throw new AssertionError("Service is running but /todos returned status " + response.statusCode);
        }
    }

    public static ObjectNode jsonObject() {
        return MAPPER.createObjectNode();
    }

    public static String toJson(ObjectNode node) {
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Failed to serialize JSON payload: " + e.getMessage(), e);
        }
    }

    public static JsonNode parseJson(String body) {
        try {
            return MAPPER.readTree(body);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }

    public static String createTodo(String title, boolean doneStatus, String description) {
        ObjectNode payload = jsonObject();
        payload.put("title", title);
        payload.put("doneStatus", doneStatus);
        payload.put("description", description);

        Response response = postJson("/todos", toJson(payload));
        assertEquals(201, response.statusCode);
        JsonNode body = parseJson(response.body);
        String id = body.path("id").asText(null);
        assertNotNull(id, "Created todo should include an id");
        return id;
    }

    public static String createCategory(String title, String description) {
        ObjectNode payload = jsonObject();
        payload.put("title", title);
        payload.put("description", description);

        Response response = postJson("/categories", toJson(payload));
        assertEquals(201, response.statusCode);
        JsonNode body = parseJson(response.body);
        String id = body.path("id").asText(null);
        assertNotNull(id, "Created category should include an id");
        return id;
    }

    public static String createProject(String title, boolean completed, boolean active, String description) {
        ObjectNode payload = jsonObject();
        payload.put("title", title);
        payload.put("completed", completed);
        payload.put("active", active);
        payload.put("description", description);

        Response response = postJson("/projects", toJson(payload));
        assertEquals(201, response.statusCode);
        JsonNode body = parseJson(response.body);
        String id = body.path("id").asText(null);
        assertNotNull(id, "Created project should include an id");
        return id;
    }

    public static final class Response {
        public final int statusCode;
        public final String body;
        public final Map<String, List<String>> headers;

        public Response(int statusCode, String body, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }

        public String header(String name) {
            if (name == null) {
                return null;
            }
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    List<String> values = entry.getValue();
                    if (values == null || values.isEmpty()) {
                        return null;
                    }
                    return values.get(0);
                }
            }
            return null;
        }
    }
}
