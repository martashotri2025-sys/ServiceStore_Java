package carService.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("CarServiceException Unit Tests")
class CarServiceExceptionTest {

    @Test
    @DisplayName("Creates CarServiceException with specified message")
    void constructor_WithMessage_ShouldRetainMessage() {
        final String errorMessage = "Specific service error occurred.";

        final CarServiceException exception = new CarServiceException(errorMessage);

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals(errorMessage, exception.getMessage());
    }
}

