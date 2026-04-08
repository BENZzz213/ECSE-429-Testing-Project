package ecse429.perf.support;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomTodoFactory {
    public TodoPayload nextPayload() {
        String token = UUID.randomUUID().toString().replace("-", "");
        String title = "PerfTodo-" + token.substring(0, 12);
        String description = "Generated for performance run " + token.substring(12);
        boolean doneStatus = ThreadLocalRandom.current().nextBoolean();
        return new TodoPayload(title, doneStatus, description);
    }

    public record TodoPayload(String title, boolean doneStatus, String description) {
    }
}
