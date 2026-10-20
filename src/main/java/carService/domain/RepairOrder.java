package carService.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "orderId")
@ToString
public class RepairOrder {

    private final Long orderId;
    private final Car targetVehicle;
    private OrderStatus currentStatus;
    private Mechanic assignedMechanic;
    private final List<ServiceWork> scheduledWorks;

    public RepairOrder(Long orderId, Car targetVehicle) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be a positive number."
            );
        }

        if (targetVehicle == null) {
            throw new IllegalArgumentException(
                    "A valid vehicle must be provided for the order."
            );
        }

        this.orderId = orderId;
        this.targetVehicle = targetVehicle;
        this.currentStatus = OrderStatus.PENDING_DIAGNOSIS;
        this.scheduledWorks = new ArrayList<>();
    }

    public List<ServiceWork> getScheduledWorks() {
        return Collections.unmodifiableList(scheduledWorks);
    }

    public void performDiagnosis() {
        verifyActiveState("perform diagnosis");

        if (currentStatus != OrderStatus.PENDING_DIAGNOSIS) {
            throw new IllegalStateException(
                    "Diagnosis is only allowed for newly created orders."
            );
        }

        currentStatus = OrderStatus.DIAGNOSED;
    }

    public void attachWork(ServiceWork work) {
        verifyActiveState("attach new work");

        if (currentStatus != OrderStatus.DIAGNOSED) {
            throw new IllegalStateException(
                    "Works can only be attached during the DIAGNOSED phase."
            );
        }

        if (work == null) {
            throw new IllegalArgumentException(
                    "Cannot attach a null work item."
            );
        }

        scheduledWorks.add(work);
    }

    public void approveByClient() {
        verifyActiveState("approve order");

        if (currentStatus != OrderStatus.DIAGNOSED) {
            throw new IllegalStateException(
                    "Order must be diagnosed before client approval."
            );
        }

        if (scheduledWorks.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot approve an order without any scheduled works."
            );
        }

        currentStatus = OrderStatus.CLIENT_APPROVED;
    }

    public void delegateToMechanic(Mechanic mechanic) {
        verifyActiveState("delegate mechanic");

        if (currentStatus != OrderStatus.CLIENT_APPROVED) {
            throw new IllegalStateException(
                    "Mechanic can only be assigned after client approval."
            );
        }

        if (mechanic == null) {
            throw new IllegalArgumentException(
                    "Mechanic reference cannot be null."
            );
        }

        if (Objects.equals(assignedMechanic, mechanic)) {
            return;
        }

        mechanic.assignToOrder();

        if (assignedMechanic != null) {
            assignedMechanic.releaseFromOrder();
        }

        assignedMechanic = mechanic;
    }

    public void startRepairProcess() {
        verifyActiveState("start repair process");

        if (currentStatus != OrderStatus.CLIENT_APPROVED) {
            throw new IllegalStateException(
                    "Order must be approved before starting."
            );
        }

        if (assignedMechanic == null) {
            throw new IllegalStateException(
                    "Cannot start repair without a delegated mechanic."
            );
        }

        currentStatus = OrderStatus.IN_PROGRESS;
    }

    public void finishRepairProcess() {
        verifyActiveState("finish repair process");

        if (currentStatus != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only in-progress orders can be finished."
            );
        }

        boolean hasPendingWorks = scheduledWorks.stream()
                .anyMatch(work -> !work.isFinished());

        if (hasPendingWorks) {
            throw new IllegalStateException(
                    "All scheduled works must be completed before finishing the order."
            );
        }

        if (assignedMechanic != null) {
            assignedMechanic.releaseFromOrder();
        }

        currentStatus = OrderStatus.SUCCESSFULLY_COMPLETED;
    }

    public void abortOrder() {
        if (currentStatus == OrderStatus.SUCCESSFULLY_COMPLETED) {
            throw new IllegalStateException(
                    "Cannot abort an already completed order."
            );
        }

        if (currentStatus.isClosed()) {
            return;
        }

        if (assignedMechanic != null) {
            assignedMechanic.releaseFromOrder();
        }

        currentStatus = OrderStatus.CANCELED;
    }

    public BigDecimal calculateGrandTotal() {
        return scheduledWorks.stream()
                .map(ServiceWork::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void verifyActiveState(String action) {
        if (currentStatus.isClosed()) {
            throw new IllegalStateException(
                    "Action '" + action
                            + "' rejected: order is already closed (Status: "
                            + currentStatus + ")."
            );
        }
    }
}