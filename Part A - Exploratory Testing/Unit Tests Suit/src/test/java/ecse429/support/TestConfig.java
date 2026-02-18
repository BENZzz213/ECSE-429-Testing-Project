package ecse429.support;

import java.time.Duration;

/**
 * Test configuration holder (base URL, timeouts, etc.).
 */
public final class TestConfig {
    public static final String BASE_URL = "http://localhost:4567";
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private TestConfig() {
        // utility class
    }

}
