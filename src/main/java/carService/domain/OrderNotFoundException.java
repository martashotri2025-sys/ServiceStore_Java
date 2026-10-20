package carService.domain;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Repair order with ID " + orderId + " was not found.");
    }
}