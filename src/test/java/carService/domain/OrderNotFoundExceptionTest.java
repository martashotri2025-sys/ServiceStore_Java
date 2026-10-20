package carService.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("OrderNotFoundException Unit Tests")
class OrderNotFoundExceptionTest {

    private static final Long ORDER_ID = Long.valueOf("order-id".length());

    @Test
    @DisplayName("Creates exception with formatted order ID message")
    void constructor_ShouldFormatMessageWithOrderId() {
        final OrderNotFoundException exception = new OrderNotFoundException(ORDER_ID);

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals(
                "Repair order with ID " + ORDER_ID + " was not found.",
                exception.getMessage()
        );
    }
}
