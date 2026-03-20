package ecse429.bdd.support;

import java.time.Duration;

/**
 * Shared configuration for the BDD suite.
 */
public final class TestConfig {
    public static final String BASE_URL = System.getProperty("todo.baseUrl", "http://localhost:4567");
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private TestConfig() {
        // utility class
    }
}
