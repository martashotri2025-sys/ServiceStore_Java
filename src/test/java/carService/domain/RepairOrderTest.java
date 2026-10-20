package carService.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RepairOrder Domain Model Unit Tests")
class RepairOrderTest {

    private static final Long VALID_ORDER_ID = Long.valueOf("501");
    private static final Long DIFFERENT_ORDER_ID = Long.valueOf("999");

    private Car car;
    private Mechanic mechanic;
    private ServiceWork sampleWork;

    @BeforeEach
    void setUp() {
        final Client client = new Client("Alexander", "Shevchenko");
        this.car = new Car(
                "1HGCR2F83HA123456",
                "Honda",
                "Accord",
                2022,
                client
        );
        this.mechanic = new Mechanic("Bogdan Melnyk");
        this.sampleWork = new ServiceWork(
                "Brake pad replacement",
                new BigDecimal("850.00"),
                new BigDecimal("400.00")
        );
    }

    private static Stream<Long> invalidOrderIds() {
        return Stream.of(
                Long.valueOf("0"),
                Long.valueOf("-1"),
                Long.valueOf("-100")
        );
    }

    @Test
    @DisplayName("Successfully creates RepairOrder with initial status and empty works")
    void createRepairOrder_WithValidData_ShouldInitializeCorrectly() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);

        assertAll(
                () -> assertEquals(VALID_ORDER_ID, order.getOrderId()),
                () -> assertSame(car, order.getTargetVehicle()),
                () -> assertEquals(
                        OrderStatus.PENDING_DIAGNOSIS,
                        order.getCurrentStatus()
                ),
                () -> assertTrue(order.getScheduledWorks().isEmpty())
        );
    }

    @ParameterizedTest(name = "Invalid order ID: {0}")
    @MethodSource("invalidOrderIds")
    @DisplayName("Rejects zero or negative order IDs")
    void createRepairOrder_WithInvalidOrderId_ShouldThrowException(
            final Long invalidOrderId
    ) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RepairOrder(invalidOrderId, car)
        );

        assertEquals(
                "Order ID must be a positive number.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects null order ID")
    void createRepairOrder_WithNullOrderId_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RepairOrder(null, car)
        );

        assertEquals(
                "Order ID must be a positive number.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects null target vehicle")
    void createRepairOrder_WithNullCar_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RepairOrder(VALID_ORDER_ID, null)
        );

        assertEquals(
                "A valid vehicle must be provided for the order.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Executes the full repair order lifecycle successfully")
    void fullLifecycle_HappyPath_ShouldSucceed() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);

        order.performDiagnosis();
        assertEquals(OrderStatus.DIAGNOSED, order.getCurrentStatus());

        order.attachWork(sampleWork);
        assertEquals(1, order.getScheduledWorks().size());

        order.approveByClient();
        assertEquals(OrderStatus.CLIENT_APPROVED, order.getCurrentStatus());

        order.delegateToMechanic(mechanic);
        assertSame(mechanic, order.getAssignedMechanic());
        assertFalse(mechanic.isAvailable());

        order.startRepairProcess();
        assertEquals(OrderStatus.IN_PROGRESS, order.getCurrentStatus());

        sampleWork.markAsFinished();
        order.finishRepairProcess();

        assertAll(
                () -> assertEquals(
                        OrderStatus.SUCCESSFULLY_COMPLETED,
                        order.getCurrentStatus()
                ),
                () -> assertTrue(mechanic.isAvailable()),
                () -> assertEquals(
                        new BigDecimal("1250.00"),
                        order.calculateGrandTotal()
                )
        );
    }

    @Test
    @DisplayName("Rejects diagnosis when order has already been diagnosed")
    void performDiagnosis_WhenAlreadyDiagnosed_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::performDiagnosis
        );

        assertEquals(
                "Diagnosis is only allowed for newly created orders.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects adding work when order is not diagnosed")
    void attachWork_WhenStatusNotDiagnosed_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.attachWork(sampleWork)
        );

        assertEquals(
                "Works can only be attached during the DIAGNOSED phase.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects null work")
    void attachWork_WithNullWork_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.attachWork(null)
        );

        assertEquals(
                "Cannot attach a null work item.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects client approval when there are no scheduled works")
    void approveByClient_WithoutWorks_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::approveByClient
        );

        assertEquals(
                "Cannot approve an order without any scheduled works.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects client approval before diagnosis")
    void approveByClient_WhenPendingDiagnosis_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::approveByClient
        );

        assertEquals(
                "Order must be diagnosed before client approval.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects mechanic assignment before client approval")
    void delegateToMechanic_BeforeApproval_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.delegateToMechanic(mechanic)
        );

        assertEquals(
                "Mechanic can only be assigned after client approval.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects null mechanic")
    void delegateToMechanic_WithNullMechanic_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.delegateToMechanic(null)
        );

        assertEquals(
                "Mechanic reference cannot be null.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Releases the previous mechanic when assigning a different mechanic")
    void delegateToMechanic_ReassigningDifferentMechanic_ShouldReleasePrevious() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(mechanic);

        final Mechanic secondMechanic = new Mechanic("Petro Sydorenko");

        order.delegateToMechanic(secondMechanic);

        assertAll(
                () -> assertTrue(mechanic.isAvailable()),
                () -> assertFalse(secondMechanic.isAvailable()),
                () -> assertSame(secondMechanic, order.getAssignedMechanic())
        );
    }

    @Test
    @DisplayName("Reassigning the same mechanic does not change assignment")
    void delegateToMechanic_ReassigningSameMechanic_ShouldBeIdempotent() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(mechanic);

        order.delegateToMechanic(mechanic);

        assertAll(
                () -> assertSame(mechanic, order.getAssignedMechanic()),
                () -> assertFalse(mechanic.isAvailable())
        );
    }

    @Test
    @DisplayName("Does not replace assigned mechanic when the new mechanic is busy")
    void delegateToMechanic_WhenNewMechanicIsBusy_ShouldKeepCurrentMechanic() {
        final RepairOrder firstOrder = new RepairOrder(VALID_ORDER_ID, car);
        final RepairOrder secondOrder = new RepairOrder(
                DIFFERENT_ORDER_ID,
                car
        );

        final Mechanic firstMechanic = new Mechanic("Bogdan Melnyk");
        final Mechanic busyMechanic = new Mechanic("Petro Sydorenko");

        firstOrder.performDiagnosis();
        firstOrder.attachWork(sampleWork);
        firstOrder.approveByClient();
        firstOrder.delegateToMechanic(firstMechanic);

        secondOrder.performDiagnosis();
        secondOrder.attachWork(
                new ServiceWork(
                        "Oil change",
                        new BigDecimal("200.00"),
                        new BigDecimal("100.00")
                )
        );
        secondOrder.approveByClient();
        secondOrder.delegateToMechanic(busyMechanic);

        assertThrows(
                IllegalStateException.class,
                () -> firstOrder.delegateToMechanic(busyMechanic)
        );

        assertAll(
                () -> assertSame(
                        firstMechanic,
                        firstOrder.getAssignedMechanic()
                ),
                () -> assertFalse(firstMechanic.isAvailable()),
                () -> assertFalse(busyMechanic.isAvailable())
        );
    }

    @Test
    @DisplayName("Rejects starting repair without an assigned mechanic")
    void startRepairProcess_WithoutMechanic_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::startRepairProcess
        );

        assertEquals(
                "Cannot start repair without a delegated mechanic.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects starting repair before client approval")
    void startRepairProcess_WhenOrderIsNotApproved_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::startRepairProcess
        );

        assertEquals(
                "Order must be approved before starting.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects finishing repair when scheduled works are unfinished")
    void finishRepairProcess_WithUnfinishedWorks_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(mechanic);
        order.startRepairProcess();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::finishRepairProcess
        );

        assertEquals(
                "All scheduled works must be completed before finishing the order.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects finishing an order that is not in progress")
    void finishRepairProcess_WhenOrderIsNotInProgress_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::finishRepairProcess
        );

        assertEquals(
                "Only in-progress orders can be finished.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Cancels an active order and releases its mechanic")
    void abortOrder_WhenInProgress_ShouldCancelAndReleaseMechanic() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(mechanic);
        order.startRepairProcess();

        order.abortOrder();

        assertAll(
                () -> assertEquals(
                        OrderStatus.CANCELED,
                        order.getCurrentStatus()
                ),
                () -> assertTrue(mechanic.isAvailable())
        );
    }

    @Test
    @DisplayName("Rejects cancellation of a completed order")
    void abortOrder_WhenAlreadyCompleted_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(mechanic);
        order.startRepairProcess();

        sampleWork.markAsFinished();
        order.finishRepairProcess();

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::abortOrder
        );

        assertEquals(
                "Cannot abort an already completed order.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Repeated cancellation of an already canceled order is safe")
    void abortOrder_WhenAlreadyCanceled_ShouldBeIdempotent() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);

        order.abortOrder();
        assertEquals(OrderStatus.CANCELED, order.getCurrentStatus());

        order.abortOrder();

        assertEquals(OrderStatus.CANCELED, order.getCurrentStatus());
    }

    @Test
    @DisplayName("Rejects actions on closed orders")
    void actionsOnClosedOrder_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.abortOrder();

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        order::performDiagnosis
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> order.attachWork(sampleWork)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        order::approveByClient
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> order.delegateToMechanic(mechanic)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        order::startRepairProcess
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        order::finishRepairProcess
                )
        );
    }

    @Test
    @DisplayName("Returns an unmodifiable scheduled works list")
    void getScheduledWorks_ShouldBeImmutable() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        final List<ServiceWork> works = order.getScheduledWorks();

        assertThrows(
                UnsupportedOperationException.class,
                () -> works.add(sampleWork)
        );
    }

    @Test
    @DisplayName("Calculates the total cost of multiple scheduled works")
    void calculateGrandTotal_WithMultipleWorks_ShouldReturnCorrectSum() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);
        order.performDiagnosis();

        final ServiceWork firstWork = new ServiceWork(
                "Work 1",
                new BigDecimal("100.00"),
                new BigDecimal("50.00")
        );
        final ServiceWork secondWork = new ServiceWork(
                "Work 2",
                new BigDecimal("200.00"),
                new BigDecimal("80.00")
        );

        order.attachWork(firstWork);
        order.attachWork(secondWork);

        assertEquals(
                new BigDecimal("430.00"),
                order.calculateGrandTotal()
        );
    }

    @Test
    @DisplayName("Returns zero when there are no scheduled works")
    void calculateGrandTotal_WithoutWorks_ShouldReturnZero() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, car);

        assertEquals(
                BigDecimal.ZERO,
                order.calculateGrandTotal()
        );
    }

    @Test
    @DisplayName("Verifies equals and hashCode based on order ID")
    void equalsAndHashCode_BasedOnOrderId() {
        final RepairOrder firstOrder = new RepairOrder(VALID_ORDER_ID, car);
        final RepairOrder secondOrder = new RepairOrder(VALID_ORDER_ID, car);
        final RepairOrder differentOrder = new RepairOrder(
                DIFFERENT_ORDER_ID,
                car
        );

        assertAll(
                () -> assertEquals(firstOrder, secondOrder),
                () -> assertEquals(
                        firstOrder.hashCode(),
                        secondOrder.hashCode()
                ),
                () -> assertNotEquals(firstOrder, differentOrder),
                () -> assertFalse(firstOrder.equals(null))
        );
    }
}
