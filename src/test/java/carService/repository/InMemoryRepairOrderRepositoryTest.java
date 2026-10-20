package carService.repository;

import carService.domain.Car;
import carService.domain.Client;
import carService.domain.RepairOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InMemoryRepairOrderRepository Unit Tests")
class InMemoryRepairOrderRepositoryTest {

    private InMemoryRepairOrderRepository repository;
    private Car sampleCar;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepairOrderRepository();

        Client client = new Client("Alexander", "Shevchenko");
        sampleCar = new Car(
                "1HGCR2F83HA123456",
                "Honda",
                "Accord",
                2022,
                client
        );
    }

    @Test
    @DisplayName("Saves and finds repair order by ID")
    void saveAndFindOrderById_WithValidOrder_ShouldReturnPersistedOrder() {
        Long orderId = 101L;
        RepairOrder order = new RepairOrder(orderId, sampleCar);

        repository.saveOrder(order);

        Optional<RepairOrder> result = repository.findOrderById(orderId);

        assertTrue(result.isPresent());
        assertSame(order, result.get());
    }

    @Test
    @DisplayName("Rejects null repair order")
    void saveOrder_WithNullOrder_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.saveOrder(null)
        );

        assertEquals(
                "Cannot save a null order or an order without an ID.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Updates existing entry when saving an order with the same ID")
    void saveOrder_WithSameId_ShouldUpdateEntry() {
        Long orderId = 102L;
        RepairOrder order = new RepairOrder(orderId, sampleCar);

        repository.saveOrder(order);
        order.performDiagnosis();
        repository.saveOrder(order);

        Optional<RepairOrder> result = repository.findOrderById(orderId);

        assertTrue(result.isPresent());
        assertSame(order, result.get());
        assertEquals(order.getCurrentStatus(), result.get().getCurrentStatus());
        assertEquals(1, repository.fetchAllOrders().size());
    }

    @Test
    @DisplayName("Replaces the existing order when another order has the same ID")
    void saveOrder_WithDifferentOrderHavingSameId_ShouldReplaceEntry() {
        Long orderId = 103L;

        RepairOrder firstOrder = new RepairOrder(orderId, sampleCar);
        RepairOrder secondOrder = new RepairOrder(orderId, sampleCar);

        repository.saveOrder(firstOrder);
        repository.saveOrder(secondOrder);

        Optional<RepairOrder> result = repository.findOrderById(orderId);

        assertTrue(result.isPresent());
        assertSame(secondOrder, result.get());
        assertEquals(1, repository.fetchAllOrders().size());
    }

    @Test
    @DisplayName("Returns empty Optional for a non-existent ID")
    void findOrderById_WithNonExistentId_ShouldReturnEmptyOptional() {
        Optional<RepairOrder> result = repository.findOrderById(9999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Returns empty Optional for null ID")
    void findOrderById_WithNullId_ShouldReturnEmptyOptional() {
        Optional<RepairOrder> result = repository.findOrderById(null);

        assertTrue(result.isEmpty());
    }

    @ParameterizedTest(name = "Invalid ID: {0}")
    @ValueSource(longs = {0L, -1L, -100L})
    @DisplayName("Returns empty Optional for non-positive IDs")
    void findOrderById_WithNonPositiveId_ShouldReturnEmptyOptional(Long invalidId) {
        Optional<RepairOrder> result = repository.findOrderById(invalidId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Returns all saved orders")
    void fetchAllOrders_WithSavedOrders_ShouldReturnAllOrders() {
        RepairOrder firstOrder = new RepairOrder(201L, sampleCar);
        RepairOrder secondOrder = new RepairOrder(202L, sampleCar);

        repository.saveOrder(firstOrder);
        repository.saveOrder(secondOrder);

        List<RepairOrder> result = repository.fetchAllOrders();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstOrder));
        assertTrue(result.contains(secondOrder));
    }

    @Test
    @DisplayName("Returns an empty list when repository is empty")
    void fetchAllOrders_WhenEmpty_ShouldReturnEmptyList() {
        List<RepairOrder> result = repository.fetchAllOrders();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Returns a separate list from the internal storage")
    void fetchAllOrders_ShouldReturnIndependentList() {
        RepairOrder order = new RepairOrder(301L, sampleCar);
        repository.saveOrder(order);

        List<RepairOrder> result = repository.fetchAllOrders();

        result.clear();

        assertTrue(repository.findOrderById(301L).isPresent());
        assertEquals(1, repository.fetchAllOrders().size());
    }

    @Test
    @DisplayName("Returns a new list on each fetch")
    void fetchAllOrders_ShouldReturnNewListEachTime() {
        repository.saveOrder(new RepairOrder(401L, sampleCar));

        List<RepairOrder> firstResult = repository.fetchAllOrders();
        List<RepairOrder> secondResult = repository.fetchAllOrders();

        assertNotSame(firstResult, secondResult);
        assertEquals(firstResult, secondResult);
    }
}

