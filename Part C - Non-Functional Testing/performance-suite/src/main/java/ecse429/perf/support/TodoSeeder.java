package ecse429.perf.support;

import java.util.ArrayList;
import java.util.List;

public final class TodoSeeder {
    private final ApiClient client;
    private final RandomTodoFactory factory;

    public TodoSeeder(final ApiClient client, final RandomTodoFactory factory) {
        this.client = client;
        this.factory = factory;
    }

    public List<String> seedToCount(final int targetCount) {
        List<String> ids = new ArrayList<>(client.listTodoIds());
        while (ids.size() < targetCount) {
            RandomTodoFactory.TodoPayload payload = factory.nextPayload();
            ids.add(client.createTodo(payload.title(), payload.doneStatus(), payload.description()));
        }
        return ids;
    }

    public List<String> createAdditionalTodos(final int count) {
        List<String> ids = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            RandomTodoFactory.TodoPayload payload = factory.nextPayload();
            ids.add(client.createTodo(payload.title(), payload.doneStatus(), payload.description()));
        }
        return ids;
    }
}
