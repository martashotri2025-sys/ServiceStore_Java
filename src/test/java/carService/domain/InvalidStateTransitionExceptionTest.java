package carService.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("InvalidStateTransitionException Unit Tests")
class InvalidStateTransitionExceptionTest {

    @Test
    @DisplayName("Creates exception with formatted error message")
    void constructor_ShouldFormatMessageCorrectly() {
        final OrderStatus status = OrderStatus.PENDING_DIAGNOSIS;
        final String action = "start repair";

        final InvalidStateTransitionException exception = new InvalidStateTransitionException(status, action);

        assertInstanceOf(IllegalStateException.class, exception);
        assertEquals("Invalid state transition: cannot start repair from current status PENDING_DIAGNOSIS.", exception.getMessage());
    }
}
