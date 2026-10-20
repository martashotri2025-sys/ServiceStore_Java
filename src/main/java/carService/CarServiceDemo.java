package carService;

import carService.domain.Car;
import carService.domain.Client;
import carService.domain.Mechanic;
import carService.domain.RepairOrder;
import carService.domain.ServiceWork;
import carService.price.DiscountRule;
import carService.price.MinimumCostRule;
import carService.price.PriceCalculator;
import carService.price.PricingRule;
import carService.repository.InMemoryRepairOrderRepository;
import carService.repository.RepairOrderRepository;
import carService.service.CarService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CarServiceDemo {

    private static final Long POSITIVE_ORDER_ID = 501L;
    private static final Long EMPTY_ORDER_ID = 502L;
    private static final Long NON_EXISTENT_ORDER_ID = 9999L;

    private static final int VEHICLE_YEAR = 2022;

    private static final String DISCOUNT_PERCENTAGE = "0.10";
    private static final String MINIMUM_COST_FLOOR = "500.00";

    private static final BigDecimal BRAKE_PADS_PARTS = new BigDecimal("850.00");
    private static final BigDecimal BRAKE_PADS_LABOR = new BigDecimal("400.00");
    private static final BigDecimal ROTOR_SURFACE_PARTS = new BigDecimal("350.00");
    private static final BigDecimal ROTOR_SURFACE_LABOR = new BigDecimal("600.00");

    private static final String CLIENT_FIRST_NAME = "Alexander";
    private static final String CLIENT_LAST_NAME = "Shevchenko";
    private static final String VEHICLE_VIN = "1HGCR2F83HA123456";
    private static final String VEHICLE_MAKE = "Honda";
    private static final String VEHICLE_MODEL = "Accord";
    private static final String MECHANIC_NAME = "Bogdan Melnyk";

    private static final String WORK_DESC_BRAKES = "Brake pad replacement";
    private static final String WORK_DESC_ROTORS = "Rotor resurfacing";

    private static final String SECTION_SEPARATOR =
            "======================================================================";
    private static final String SUB_SEPARATOR =
            "----------------------------------------------------------------------";

    private static final String HEADER_SYSTEM_TITLE =
            "       DOMAIN-DRIVEN CAR SERVICE MANAGEMENT SYSTEM DEMONSTRATION      ";
    private static final String HEADER_DI_SETUP =
            "STEP 1: Dependency Injection Setup (Composition Root)";
    private static final String HEADER_POSITIVE_SCENARIO =
            "STEP 2: Positive Scenario - Complete Order Lifecycle & Commercial Pricing";
    private static final String HEADER_NEGATIVE_EMPTY_ORDER =
            "STEP 3: Negative Scenario - Attempting to Approve Empty Order";
    private static final String HEADER_NEGATIVE_MISSING_ORDER =
            "STEP 4: Negative Scenario - Attempting to Retrieve Non-Existent Order";
    private static final String HEADER_NEGATIVE_INVALID_TRANSITION =
            "STEP 5: Negative Scenario - Attempting Prohibited State Transition";
    private static final String HEADER_VERIFICATION_COMPLETE =
            "   ALL SCENARIOS EXECUTED GRACEFULLY WITH ZERO UNHANDLED CRASHES      ";

    private static final String LOG_REPO_INITIALIZED =
            "[DI] Initialized repository.InMemoryRepairOrderRepository.";
    private static final String LOG_PRICING_INITIALIZED =
            "[DI] Configured PricingRules (10% Discount, $500.00 Minimum Cost Floor).";
    private static final String LOG_SERVICE_INITIALIZED =
            "[DI] Injected dependencies into service.CarService application service.";
    private static final String LOG_EXPECTED_EXCEPTION_PREFIX =
            "[EXPECTED EXCEPTION CAUGHT]: ";
    private static final String LOG_GRACEFUL_CONTINUATION =
            "[RECOVERY] System recovered gracefully; state invariants preserved.";

    public static void main(final String[] args) {
        System.out.println(SECTION_SEPARATOR);
        System.out.println(HEADER_SYSTEM_TITLE);
        System.out.println(SECTION_SEPARATOR);

        printHeader(HEADER_DI_SETUP);

        final RepairOrderRepository repository = new InMemoryRepairOrderRepository();
        System.out.println(LOG_REPO_INITIALIZED);

        final List<PricingRule> pricingRules = List.of(
                new DiscountRule(DISCOUNT_PERCENTAGE),
                new MinimumCostRule(MINIMUM_COST_FLOOR)
        );
        final PriceCalculator priceCalculator = new PriceCalculator(pricingRules);
        System.out.println(LOG_PRICING_INITIALIZED);

        final CarService carService = new CarService(repository, priceCalculator);
        System.out.println(LOG_SERVICE_INITIALIZED);

        final Client client = new Client(CLIENT_FIRST_NAME, CLIENT_LAST_NAME);
        final Car car = new Car(VEHICLE_VIN, VEHICLE_MAKE, VEHICLE_MODEL, VEHICLE_YEAR, client);
        final Mechanic mechanic = new Mechanic(MECHANIC_NAME);

        System.out.println("[Domain] Registered Client: " + client.getFirstName() + " " + client.getLastName());
        System.out.println("[Domain] Registered Car: " + car.getMake() + " " + car.getModel() + " [VIN: " + car.getVin() + "]");
        System.out.println("[Domain] Registered Mechanic: " + mechanic.getFullName() + " (Available: " + mechanic.isAvailable() + ")");

        printHeader(HEADER_POSITIVE_SCENARIO);

        final RepairOrder order = new RepairOrder(POSITIVE_ORDER_ID, car);
        carService.registerRepairOrder(order);
        System.out.println("[Step 1] Order #" + order.getOrderId() + " created. Initial status: " + order.getCurrentStatus());

        order.performDiagnosis();
        System.out.println("[Step 2] Diagnosis performed. Current status: " + order.getCurrentStatus());

        final ServiceWork work1 = new ServiceWork(WORK_DESC_BRAKES, BRAKE_PADS_PARTS, BRAKE_PADS_LABOR);
        final ServiceWork work2 = new ServiceWork(WORK_DESC_ROTORS, ROTOR_SURFACE_PARTS, ROTOR_SURFACE_LABOR);
        order.attachWork(work1);
        order.attachWork(work2);
        System.out.println("[Step 3] Added 2 work items. Total cost: " + order.calculateGrandTotal());

        order.approveByClient();
        System.out.println("[Step 4] Order approved by client. Current status: " + order.getCurrentStatus());

        order.delegateToMechanic(mechanic);
        System.out.println("[Step 5] Assigned mechanic: " + order.getAssignedMechanic().getFullName()
                + " (Available: " + mechanic.isAvailable() + ")");

        order.startRepairProcess();
        System.out.println("[Step 6] Execution started. Current status: " + order.getCurrentStatus());

        work1.markAsFinished();
        work2.markAsFinished();
        System.out.println("[Step 7] All service work items marked as completed.");

        order.finishRepairProcess();
        System.out.println("[Step 8] Order successfully completed! Current status: " + order.getCurrentStatus());
        System.out.println("         Assigned mechanic automatically released (Available: " + mechanic.isAvailable() + ")");

        final BigDecimal basicTotalCost = order.calculateGrandTotal();
        final BigDecimal commercialPrice = carService.processCommercialInvoice(order.getOrderId());
        System.out.println("[Step 9] Basic Total Cost (parts + labor): $" + basicTotalCost);
        System.out.println("         Commercial Price (after 10% discount): $" + commercialPrice);

        final RepairOrder persistedOrder = carService.retrieveOrder(order.getOrderId());
        System.out.println("[Step 10] Verified in repository: Order #" + persistedOrder.getOrderId()
                + " stored with status: " + persistedOrder.getCurrentStatus());

        printHeader(HEADER_NEGATIVE_EMPTY_ORDER);

        final RepairOrder emptyOrder = new RepairOrder(EMPTY_ORDER_ID, car);
        carService.registerRepairOrder(emptyOrder);
        emptyOrder.performDiagnosis();
        System.out.println("Created order #" + emptyOrder.getOrderId() + " in status " + emptyOrder.getCurrentStatus()
                + " with 0 assigned works.");

        try {
            System.out.println("Attempting to approve order without work items...");
            emptyOrder.approveByClient();
            System.err.println("[TEST FAILED] Approval should have been rejected!");
        } catch (final IllegalStateException ex) {
            System.out.println(LOG_EXPECTED_EXCEPTION_PREFIX + ex.getMessage());
            System.out.println("Verified order state was not corrupted: " + emptyOrder.getCurrentStatus());
            System.out.println(LOG_GRACEFUL_CONTINUATION);
        }

        printHeader(HEADER_NEGATIVE_MISSING_ORDER);

        System.out.println("Querying repository for non-existent order #" + NON_EXISTENT_ORDER_ID + "...");
        final Optional<RepairOrder> missingOpt = repository.findOrderById(NON_EXISTENT_ORDER_ID);
        System.out.println("Result: " + (missingOpt.isEmpty() ? "Optional.empty() returned as expected." : "Found unexpected order!"));

        try {
            System.out.println("Attempting to retrieve non-existent order #" + NON_EXISTENT_ORDER_ID + "...");
            carService.retrieveOrder(NON_EXISTENT_ORDER_ID);
            System.err.println("[TEST FAILED] Querying missing order should have been rejected!");
        } catch (final carService.service.CarServiceException | IllegalArgumentException ex) {
            System.out.println(LOG_EXPECTED_EXCEPTION_PREFIX + ex.getMessage());
            System.out.println(LOG_GRACEFUL_CONTINUATION);
        }

        printHeader(HEADER_NEGATIVE_INVALID_TRANSITION);

        System.out.println("Attempting to directly complete an order in DIAGNOSED state (skipping IN_PROGRESS)...");
        try {
            emptyOrder.finishRepairProcess();
            System.err.println("[TEST FAILED] Direct completion from DIAGNOSED should have failed!");
        } catch (final IllegalStateException ex) {
            System.out.println(LOG_EXPECTED_EXCEPTION_PREFIX + ex.getMessage());
            System.out.println("Order status remained safely at: " + emptyOrder.getCurrentStatus());
            System.out.println(LOG_GRACEFUL_CONTINUATION);
        }

        System.out.println("\n" + SECTION_SEPARATOR);
        System.out.println(HEADER_VERIFICATION_COMPLETE);
        System.out.println(SECTION_SEPARATOR);
    }

    private static void printHeader(final String title) {
        System.out.println("\n" + SUB_SEPARATOR);
        System.out.println(title);
        System.out.println(SUB_SEPARATOR);
    }
}
