package ecse429.todos;

import ecse429.support.TestSupport;
import ecse429.support.TestSupport.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Service availability requirement.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class ServiceAvailabilityTests {

    @Test
    // Tests should fail if the service is not running (GET /todos must return 200).
    void serviceMustBeRunningForTests() {
        Response response = TestSupport.get("/todos");
        assertEquals(200, response.statusCode);
    }
}
