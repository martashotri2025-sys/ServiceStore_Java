package carService.domain;

public class InvalidStateTransitionException extends IllegalStateException {
    public InvalidStateTransitionException(OrderStatus currentStatus, String action) {
        super("Invalid state transition: cannot " + action + " from current status " + currentStatus + ".");
    }
}