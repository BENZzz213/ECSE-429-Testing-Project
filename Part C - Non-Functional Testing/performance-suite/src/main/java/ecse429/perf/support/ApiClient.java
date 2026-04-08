package ecse429.perf.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ApiClient {
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"?([^\",}]+)\"?");

    private final HttpClient client;
    private final String baseUrl;

    public ApiClient(final String baseUrl) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Response get(final String path) {
        return request("GET", path, null);
    }

    public Response postJson(final String path, final String jsonBody) {
        return request("POST", path, jsonBody);
    }

    public Response putJson(final String path, final String jsonBody) {
        return request("PUT", path, jsonBody);
    }

    public Response delete(final String path) {
        return request("DELETE", path, null);
    }

    public void requireServiceUp() {
        Response response = get("/todos");
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Expected GET /todos to return 200 but got " + response.statusCode());
        }
    }

    public List<String> listTodoIds() {
        Response response = get("/todos");
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to read /todos. Status: " + response.statusCode());
        }

        List<String> ids = new ArrayList<>();
        Matcher matcher = ID_PATTERN.matcher(response.body());
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    public String createTodo(final String title, final boolean doneStatus, final String description) {
        String payload = "{\"title\":\"" + escapeJson(title) + "\","
                + "\"doneStatus\":" + doneStatus + ","
                + "\"description\":\"" + escapeJson(description) + "\"}";

        Response response = postJson("/todos", payload);
        if (response.statusCode() != 201) {
            throw new IllegalStateException("Expected create to return 201 but got "
                    + response.statusCode() + " with body: " + response.body());
        }
        return extractSingleId(response.body());
    }

    public void updateTodo(final String todoId, final String title, final boolean doneStatus, final String description) {
        String payload = "{\"title\":\"" + escapeJson(title) + "\","
                + "\"doneStatus\":" + doneStatus + ","
                + "\"description\":\"" + escapeJson(description) + "\"}";

        Response response = putJson("/todos/" + todoId, payload);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Expected update to return 200 but got "
                    + response.statusCode() + " for todo " + todoId + " with body: " + response.body());
        }
    }

    public void deleteTodo(final String todoId) {
        Response response = delete("/todos/" + todoId);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Expected delete to return 200 but got "
                    + response.statusCode() + " for todo " + todoId + " with body: " + response.body());
        }
    }

    private Response request(final String method, final String path, final String body) {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .method(method, publisher)
                .header("Accept", "application/json");

        if (body != null) {
            builder.header("Content-Type", "application/json");
        }

        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new Response(response.statusCode(), response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("HTTP request failed: " + method + " " + path, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("HTTP request interrupted: " + method + " " + path, exception);
        }
    }

    private static String stripTrailingSlash(final String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String extractSingleId(final String body) {
        Matcher matcher = ID_PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("Response body did not contain an id: " + body);
        }
        return matcher.group(1);
    }

    private static String escapeJson(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public record Response(int statusCode, String body) {
    }
}
