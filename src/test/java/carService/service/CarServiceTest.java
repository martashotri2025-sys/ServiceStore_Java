package carService.service;

import carService.domain.Car;
import carService.domain.Client;
import carService.domain.Mechanic;
import carService.domain.OrderStatus;
import carService.domain.RepairOrder;
import carService.domain.ServiceWork;
import carService.price.PriceCalculator;
import carService.repository.RepairOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarService Application Service Unit Tests with Mockito")
class CarServiceTest {

    private static final Long VALID_ORDER_ID = 501L;

    @Mock
    private RepairOrderRepository repository;

    @Mock
    private PriceCalculator priceCalculator;

    @InjectMocks
    private CarService carService;

    private Car sampleCar;
    private Mechanic sampleMechanic;
    private ServiceWork sampleWork;

    @BeforeEach
    void setUp() {
        final Client client = new Client("Alexander", "Shevchenko");
        this.sampleCar = new Car("1HGCR2F83HA123456", "Honda", "Accord", 2022, client);
        this.sampleMechanic = new Mechanic("Bogdan Melnyk");
        this.sampleWork = new ServiceWork("Brake pad replacement", new BigDecimal("850.00"), new BigDecimal("400.00"));
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException when repository dependency is null")
    void constructor_WithNullRepository_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CarService(null, priceCalculator)
        );
        assertEquals("Repository dependency is required.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException when price calculator dependency is null")
    void constructor_WithNullPriceCalculator_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CarService(repository, null)
        );
        assertEquals("Price calculator dependency is required.", exception.getMessage());
    }

    @Test
    @DisplayName("registerRepairOrder successfully delegates to repository")
    void registerRepairOrder_WithValidOrder_ShouldDelegateToRepository() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);

        carService.registerRepairOrder(order);

        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("registerRepairOrder throws IllegalArgumentException and does not interact with repository when order is null")
    void registerRepairOrder_WithNullOrder_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carService.registerRepairOrder(null)
        );
        assertEquals("Order cannot be null.", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("retrieveOrder returns order when found in repository")
    void retrieveOrder_WhenOrderExists_ShouldReturnOrder() {
        final RepairOrder expectedOrder = new RepairOrder(VALID_ORDER_ID, sampleCar);
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(expectedOrder));

        final RepairOrder retrievedOrder = carService.retrieveOrder(VALID_ORDER_ID);

        assertSame(expectedOrder, retrievedOrder);
        verify(repository).findOrderById(VALID_ORDER_ID);
    }

    @Test
    @DisplayName("retrieveOrder throws IllegalArgumentException when orderId is null")
    void retrieveOrder_WithNullOrderId_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carService.retrieveOrder(null)
        );
        assertEquals("Order ID cannot be null.", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("retrieveOrder throws CarServiceException when order does not exist")
    void retrieveOrder_WhenOrderNotFound_ShouldThrowCarServiceException() {
        final Long missingOrderId = 999L;
        when(repository.findOrderById(missingOrderId)).thenReturn(Optional.empty());

        final CarServiceException exception = assertThrows(
                CarServiceException.class,
                () -> carService.retrieveOrder(missingOrderId)
        );
        assertEquals("Repair order with ID 999 does not exist.", exception.getMessage());
        verify(repository).findOrderById(missingOrderId);
    }

    @Test
    @DisplayName("diagnoseOrder updates order status to DIAGNOSED and saves to repository")
    void diagnoseOrder_WhenValid_ShouldPerformDiagnosisAndSave() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));

        carService.diagnoseOrder(VALID_ORDER_ID);

        assertEquals(OrderStatus.DIAGNOSED, order.getCurrentStatus());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("addWorkToOrder attaches work and saves order to repository")
    void addWorkToOrder_WhenValid_ShouldAttachWorkAndSave() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        order.performDiagnosis();
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));

        carService.addWorkToOrder(VALID_ORDER_ID, sampleWork);

        assertEquals(1, order.getScheduledWorks().size());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("approveOrder transitions order to CLIENT_APPROVED and saves")
    void approveOrder_WhenValid_ShouldApproveAndSave() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));

        carService.approveOrder(VALID_ORDER_ID);

        assertEquals(OrderStatus.CLIENT_APPROVED, order.getCurrentStatus());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("assignMechanicToOrder assigns mechanic and saves")
    void assignMechanicToOrder_WhenValid_ShouldAssignAndSave() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));

        carService.assignMechanicToOrder(VALID_ORDER_ID, sampleMechanic);

        assertSame(sampleMechanic, order.getAssignedMechanic());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("startOrderRepair transitions order to IN_PROGRESS and saves")
    void startOrderRepair_WhenValid_ShouldStartRepairAndSave() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(sampleMechanic);
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));

        carService.startOrderRepair(VALID_ORDER_ID);

        assertEquals(OrderStatus.IN_PROGRESS, order.getCurrentStatus());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("finishOrderRepair transitions order to SUCCESSFULLY_COMPLETED and saves")
    void finishOrderRepair_WhenValid_ShouldFinishRepairAndSave() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(sampleMechanic);
        order.startRepairProcess();
        sampleWork.markAsFinished();
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));

        carService.finishOrderRepair(VALID_ORDER_ID);

        assertEquals(OrderStatus.SUCCESSFULLY_COMPLETED, order.getCurrentStatus());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("calculateInvoiceAmount delegates grand total calculation to PriceCalculator mock")
    void calculateInvoiceAmount_WhenOrderCompleted_ShouldCalculateViaPriceCalculator() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(sampleMechanic);
        order.startRepairProcess();
        sampleWork.markAsFinished();
        order.finishRepairProcess();

        final BigDecimal baseTotal = order.calculateGrandTotal();
        final BigDecimal expectedCommercialPrice = new BigDecimal("1125.00");

        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));
        when(priceCalculator.calculateCommercialPrice(baseTotal)).thenReturn(expectedCommercialPrice);

        final BigDecimal invoiceAmount = carService.calculateInvoiceAmount(VALID_ORDER_ID);

        assertEquals(expectedCommercialPrice, invoiceAmount);
        verify(priceCalculator).calculateCommercialPrice(baseTotal);
    }

    @Test
    @DisplayName("processCommercialInvoice delegates correctly to calculateInvoiceAmount")
    void processCommercialInvoice_ShouldReturnCommercialAmount() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        order.performDiagnosis();
        order.attachWork(sampleWork);
        order.approveByClient();
        order.delegateToMechanic(sampleMechanic);
        order.startRepairProcess();
        sampleWork.markAsFinished();
        order.finishRepairProcess();

        final BigDecimal baseTotal = order.calculateGrandTotal();
        final BigDecimal expectedPrice = new BigDecimal("1125.00");

        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));
        when(priceCalculator.calculateCommercialPrice(baseTotal)).thenReturn(expectedPrice);

        final BigDecimal invoiceAmount = carService.processCommercialInvoice(VALID_ORDER_ID);

        assertEquals(expectedPrice, invoiceAmount);
        verify(priceCalculator).calculateCommercialPrice(baseTotal);
    }

    @Test
    @DisplayName("calculateInvoiceAmount throws IllegalStateException when order is not completed")
    void calculateInvoiceAmount_WhenOrderNotCompleted_ShouldThrowException() {
        final RepairOrder order = new RepairOrder(VALID_ORDER_ID, sampleCar);
        when(repository.findOrderById(VALID_ORDER_ID)).thenReturn(Optional.of(order));

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> carService.calculateInvoiceAmount(VALID_ORDER_ID)
        );
        assertEquals("Invoice can only be calculated for successfully completed orders.", exception.getMessage());
        verify(priceCalculator, never()).calculateCommercialPrice(any());
    }
}

