import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Service order (ServiceOrder).
 * Domain entity with full encapsulation and business operations.
 * State transitions occur exclusively through business logic domain methods.
 */
public class ServiceOrder {

    private final Long id;
    private Car car;
    private final List<ServiceWork> works;
    private Mechanic mechanic;
    private OrderStatus status;

    public ServiceOrder() {
        this.id = null;
        this.car = null;
        this.works = new ArrayList<>();
        this.mechanic = null;
        this.status = OrderStatus.CREATED;
    }

    public ServiceOrder(final Long id, final Car car) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number and cannot be null.");
        }
        this.id = id;
        setCar(car);
        this.works = new ArrayList<>();
        this.mechanic = null;
        this.status = OrderStatus.CREATED;
    }

    public void setCar(final Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Order vehicle cannot be null.");
        }
        this.car = car;
    }

    // --- Business Operations ---

    /**
     * Adds a work item to the order.
     * Forbidden if the order is already completed or cancelled.
     */
    public void addWork(final ServiceWork work) {
        ensureNotFinished("add work");
        if (work == null) {
            throw new IllegalArgumentException("Work cannot be null.");
        }
        this.works.add(work);
    }

    /**
     * Vehicle diagnosis.
     * Transition: CREATED -> DIAGNOSED.
     */
    public void diagnose() {
        ensureNotFinished("perform diagnosis");
        if (this.status != OrderStatus.CREATED) {
            throw new CarServiceException(
                    "Invalid state transition: diagnosis can only be performed from CREATED status (current: " + this.status + ")."
            );
        }
        this.status = OrderStatus.DIAGNOSED;
    }

    /**
     * Order approval by client.
     * Transition: DIAGNOSED -> APPROVED.
     * Business rule: cannot approve an order without added works (empty).
     */
    public void approve() {
        ensureNotFinished("approve order");
        if (this.status != OrderStatus.DIAGNOSED) {
            throw new CarServiceException(
                    "Invalid state transition: order can only be approved after diagnosis from DIAGNOSED status (current: " + this.status + ")."
            );
        }
        if (this.works.isEmpty()) {
            throw new CarServiceException(
                    "Validation error: cannot approve an empty order with no assigned works."
            );
        }
        this.status = OrderStatus.APPROVED;
    }

    /**
     * Assigns a mechanic to the order.
     * Business rule: cannot assign a busy mechanic or modify a completed order.
     */
    public void assignMechanic(final Mechanic newMechanic) {
        ensureNotFinished("assign mechanic");
        if (newMechanic == null) {
            throw new IllegalArgumentException("Mechanic cannot be null.");
        }

        // If the mechanic is already the same, do nothing
        if (Objects.equals(this.mechanic, newMechanic)) {
            return;
        }

        // Attempt to assign the mechanic (throws CarServiceException if already busy)
        newMechanic.assign();

        // If another mechanic was previously assigned, release them
        if (this.mechanic != null) {
            this.mechanic.release();
        }

        this.mechanic = newMechanic;
    }

    /**
     * Starts execution of the order.
     * Transition: APPROVED -> IN_PROGRESS.
     * Business rule: cannot start without an assigned mechanic.
     */
    public void startExecution() {
        ensureNotFinished("start execution");
        if (this.status != OrderStatus.APPROVED) {
            throw new CarServiceException(
                    "Invalid state transition: execution can only start from an approved order in APPROVED status (current: " + this.status + ")."
            );
        }
        if (this.mechanic == null) {
            throw new CarServiceException(
                    "Resource error: cannot start execution of an order without an assigned mechanic."
            );
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    /**
     * Completes an individual work item.
     */
    public void completeWork(final ServiceWork work) {
        ensureNotFinished("complete work");
        if (this.status != OrderStatus.IN_PROGRESS) {
            throw new CarServiceException(
                    "Works can only be executed in IN_PROGRESS status (current: " + this.status + ")."
            );
        }
        if (work == null || !this.works.contains(work)) {
            throw new IllegalArgumentException("The specified work does not belong to this order.");
        }
        work.markAsCompleted();
    }

    /**
     * Marks all works in the order as completed.
     */
    public void markAllWorksCompleted() {
        ensureNotFinished("complete works");
        if (this.status != OrderStatus.IN_PROGRESS) {
            throw new CarServiceException(
                    "Works can only be completed in IN_PROGRESS status (current: " + this.status + ")."
            );
        }
        for (final ServiceWork work : this.works) {
            if (!work.isCompleted()) {
                work.markAsCompleted();
            }
        }
    }

    /**
     * Completes the order.
     * Transition: IN_PROGRESS -> COMPLETED.
     * Business rule: all works must be completed. Releases the mechanic.
     */
    public void completeOrder() {
        ensureNotFinished("complete order");
        if (this.status != OrderStatus.IN_PROGRESS) {
            throw new CarServiceException(
                    "Invalid state transition: cannot complete order from status " + this.status + " (expected IN_PROGRESS)."
            );
        }
        final boolean hasIncompleteWorks = this.works.stream().anyMatch(w -> !w.isCompleted());
        if (hasIncompleteWorks) {
            throw new CarServiceException(
                    "Validation error: cannot complete order until all works are completed."
            );
        }
        this.status = OrderStatus.COMPLETED;
        if (this.mechanic != null) {
            this.mechanic.release();
        }
    }

    /**
     * Cancels the order.
     * Transition: any incomplete state -> CANCELLED.
     */
    public void cancelOrder() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new CarServiceException("Cannot cancel order because it is already completed.");
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new CarServiceException("Order is already cancelled.");
        }
        this.status = OrderStatus.CANCELLED;
        if (this.mechanic != null) {
            this.mechanic.release();
        }
    }

    /**
     * Calculates the total cost of the order (parts + labor) using the Streams API.
     */
    public BigDecimal calculateTotalCost() {
        return this.works.stream()
                .map(ServiceWork::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Helper Encapsulation Guards ---

    private void ensureNotFinished(final String actionDescription) {
        if (this.status == OrderStatus.COMPLETED) {
            throw new CarServiceException(
                    "Business rule violation: cannot " + actionDescription + " because the order is already COMPLETED."
            );
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new CarServiceException(
                    "Business rule violation: cannot " + actionDescription + " because the order is CANCELLED."
            );
        }
    }

    // --- Getters (Read-only, no generic setter methods!) ---

    public Long getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public List<ServiceWork> getWorks() {
        return Collections.unmodifiableList(works);
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalCost() {
        return calculateTotalCost();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ServiceOrder that = (ServiceOrder) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ServiceOrder{" +
                "id=" + id +
                ", car=" + car.getVin() +
                ", works=" + works.size() +
                ", mechanic=" + (mechanic != null ? mechanic.getName() : "unassigned") +
                ", status=" + status +
                ", totalCost=" + calculateTotalCost() +
                '}';
    }
}
