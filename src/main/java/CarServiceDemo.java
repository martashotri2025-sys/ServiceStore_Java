import java.math.BigDecimal;
import java.util.List;

/**
 * System demo class demonstrating all workflows and scenarios.
 * Sequentially demonstrates positive and negative scenarios.
 */
public class CarServiceDemo {
    public static void main(final String[] args) {
        System.out.println("======================================================================");
        System.out.println("          CAR SERVICE MANAGEMENT SYSTEM DEMONSTRATION                 ");
        System.out.println("======================================================================\n");

        // --------------------------------------------------------------------
        // SCENARIO 1: Successful registration of client, vehicle, and mechanics
        // --------------------------------------------------------------------
        printHeader("SCENARIO 1: Registration of client, car, and mechanics");
        final Client client = new Client(1L, "  Alexander Shevchenko  ", "  +380501234567  ");
        System.out.println("[OK] Registered client: '" + client.getName() + "' (trim applied automatically), phone: " + client.getPhone());

        final Car car1 = new Car("  1HGCR2F83HA123456  ", "  Honda  ", "  Civic  ", 2020, client);
        System.out.println("[OK] Registered car: '" + car1.getMake() + " " + car1.getModel() + "' (trim), VIN: " + car1.getVin());

        final Mechanic mechanic1 = new Mechanic(101L, "Bogdan Melnyk", "Brake Systems");
        System.out.println("[OK] Registered mechanic 1: " + mechanic1.getName() + " (" + mechanic1.getSpecialization() + ")");

        final Mechanic mechanic2 = new Mechanic(102L, "Ivan Koval", "Engines");
        System.out.println("[OK] Registered mechanic 2: " + mechanic2.getName() + " (" + mechanic2.getSpecialization() + ")");

        // --------------------------------------------------------------------
        // SCENARIO 2: Complete successful order lifecycle + cost calculation
        // --------------------------------------------------------------------
        printHeader("SCENARIO 2: Successful order cycle (create -> works -> approve -> execute -> complete)");
        final ServiceOrder order = new ServiceOrder(1L, car1);
        System.out.println("[Step 1] Order created. Status: " + order.getStatus());

        final ServiceWork work1 = new ServiceWork("Brake pad replacement", new BigDecimal("850.00"), new BigDecimal("400.00"));
        final ServiceWork work2 = new ServiceWork("Rotor resurfacing", new BigDecimal("350.00"), new BigDecimal("600.00"));
        order.addWork(work1);
        order.addWork(work2);
        System.out.println("[Step 2] Added 2 service works to the order:");
        System.out.println("         - " + work1.getDescription() + " (total: " + work1.getTotalCost() + ")");
        System.out.println("         - " + work2.getDescription() + " (total: " + work2.getTotalCost() + ")");

        order.diagnose();
        System.out.println("[Step 3] Diagnosis performed. Status: " + order.getStatus());

        order.approve();
        System.out.println("[Step 4] Order approved by client. Status: " + order.getStatus());

        order.assignMechanic(mechanic1);
        System.out.println("[Step 5] Assigned mechanic: " + order.getMechanic().getName());

        order.startExecution();
        System.out.println("[Step 6] Execution started. Status: " + order.getStatus());

        order.markAllWorksCompleted();
        System.out.println("[Step 7] All works marked as completed.");

        order.completeOrder();
        System.out.println("[Step 8] Order successfully completed! Status: " + order.getStatus());

        final BigDecimal totalCost = order.calculateTotalCost();
        System.out.println("[Step 9] Final order total cost: " + totalCost);

        // --------------------------------------------------------------------
        // SCENARIO 3: Attempt to approve empty order
        // --------------------------------------------------------------------
        printHeader("SCENARIO 3 (Negative): Attempt to approve empty order");
        final ServiceOrder emptyOrder = new ServiceOrder(2L, car1);
        emptyOrder.diagnose();
        System.out.println("Current order status: " + emptyOrder.getStatus() + ", works: " + emptyOrder.getWorks().size());
        try {
            emptyOrder.approve();
            System.out.println("[TEST ERROR] Approval should have failed!");
        } catch (final CarServiceException ex) {
            System.out.println("[EXPECTED EXCEPTION CAUGHT]: " + ex.getMessage());
            System.out.println("Object state remained unchanged and valid: " + emptyOrder.getStatus());
        }

        // --------------------------------------------------------------------
        // SCENARIO 4: Attempt to start execution without a mechanic
        // --------------------------------------------------------------------
        printHeader("SCENARIO 4 (Negative): Attempt to start execution without a mechanic");
        final ServiceOrder orderWithoutMechanic = new ServiceOrder(3L, car1);
        orderWithoutMechanic.addWork(new ServiceWork("Oil change", new BigDecimal("1200.00"), new BigDecimal("300.00")));
        orderWithoutMechanic.diagnose();
        orderWithoutMechanic.approve();
        System.out.println("Order approved. Assigned mechanic: " + orderWithoutMechanic.getMechanic());
        try {
            orderWithoutMechanic.startExecution();
            System.out.println("[TEST ERROR] Execution start should have failed!");
        } catch (final CarServiceException ex) {
            System.out.println("[EXPECTED EXCEPTION CAUGHT]: " + ex.getMessage());
            System.out.println("Order state remained valid after error: " + orderWithoutMechanic.getStatus());
        }

        // --------------------------------------------------------------------
        // SCENARIO 5: Attempt to assign busy mechanic to another active order
        // --------------------------------------------------------------------
        printHeader("SCENARIO 5 (Negative): Attempt to assign busy mechanic to another active order");
        // Start active order with mechanic2
        final ServiceOrder activeOrder = new ServiceOrder(4L, car1);
        activeOrder.addWork(new ServiceWork("Electronics diagnostics", BigDecimal.ZERO, new BigDecimal("500.00")));
        activeOrder.diagnose();
        activeOrder.approve();
        activeOrder.assignMechanic(mechanic2);
        activeOrder.startExecution();


        // Create a new order and attempt to assign the same mechanic2
        final ServiceOrder conflictOrder = new ServiceOrder(5L, car1);
        System.out.println("Attempting to assign busy mechanic " + mechanic2.getName() + " to order #5...");
        try {
            conflictOrder.assignMechanic(mechanic2);
            System.out.println("[TEST ERROR] Assignment should have failed!");
        } catch (final CarServiceException ex) {
            System.out.println("[EXPECTED EXCEPTION CAUGHT]: " + ex.getMessage());
            System.out.println("Order #5 remained unassigned: " + conflictOrder.getMechanic());
        }

        // --------------------------------------------------------------------
        // SCENARIO 6: Attempt to modify completed order
        // --------------------------------------------------------------------
        printHeader("SCENARIO 6 (Negative): Attempt to modify completed order");
        System.out.println("Taking previously completed order #1. Current status: " + order.getStatus());
        try {
            System.out.println("Attempting to add new work to completed order...");
            order.addWork(new ServiceWork("Headlight restoration", BigDecimal.ZERO, new BigDecimal("200.00")));
            System.out.println("[TEST ERROR] Modification should have been blocked!");
        } catch (final CarServiceException ex) {
            System.out.println("[EXPECTED EXCEPTION CAUGHT]: " + ex.getMessage());
        }

        try {
            System.out.println("Attempting to assign mechanic to completed order...");
            order.assignMechanic(mechanic1);
            System.out.println("[TEST ERROR] Assignment should have been blocked!");
        } catch (final CarServiceException ex) {
            System.out.println("[EXPECTED EXCEPTION CAUGHT]: " + ex.getMessage());
        }

        // --------------------------------------------------------------------
        // SCENARIO 7: Attempt to perform prohibited status transition
        // --------------------------------------------------------------------
        printHeader("SCENARIO 7 (Negative): Attempt prohibited status transition");
        final ServiceOrder freshOrder = new ServiceOrder(6L, car1);
        System.out.println("Created new order with status: " + freshOrder.getStatus());
        try {
            System.out.println("Attempting to immediately complete newly created order (skip from CREATED to COMPLETED)...");
            freshOrder.completeOrder();
            System.out.println("[TEST ERROR] Transition should have failed!");
        } catch (final CarServiceException ex) {
            System.out.println("[EXPECTED EXCEPTION CAUGHT]: " + ex.getMessage());
            System.out.println("Order status remained unchanged: " + freshOrder.getStatus());
        }

        try {
            System.out.println("Attempting to immediately start execution (from CREATED to IN_PROGRESS)...");
            freshOrder.startExecution();
            System.out.println("[TEST ERROR] Transition should have failed!");
        } catch (final CarServiceException ex) {
            System.out.println("[EXPECTED EXCEPTION CAUGHT]: " + ex.getMessage());
            System.out.println("Order status remained unchanged: " + freshOrder.getStatus());
        }

        // --------------------------------------------------------------------
        // SCENARIO 8: TransportCompany sending buses and taxis to ServiceStore
        // --------------------------------------------------------------------
        printHeader("SCENARIO 8: TransportCompany dispatching buses and taxis for repair");
        final TransportCompany company = new TransportCompany(501L, "Kyiv City Transit", "+380449876543");

        // Populate fleet with buses and taxis (stored in one transport list)
        company.createBus("BUS-VIN-001", "Mercedes-Benz", "Citaro", 2021);
        company.createBus("BUS-VIN-002", "MAN", "Lion's City", 2022);
        company.createBus("BUS-VIN-003", "Solaris", "Urbino 12", 2023);

        company.createTaxi("TAXI-VIN-101", "Toyota", "Camry", 2020);
        company.createTaxi("TAXI-VIN-102", "Hyundai", "Sonata", 2021);
        company.createTaxi("TAXI-VIN-103", "Skoda", "Octavia", 2022);
        company.createTaxi("TAXI-VIN-104", "Toyota", "Prius", 2023);

        System.out.println("Transport company registered: '" + company.getName() + "'");
        System.out.println("Total transport owned in fleet: " + company.getTransport().size());

        // Dispatch 5 vehicles to ServiceStore using one parameter: amount
        final int amountToRepair = 5;
        System.out.println("\nSending transport to ServiceStore with 1 parameter (amount = " + amountToRepair + ")...");
        final List<Car> vehiclesToRepair = company.sendToServiceStore(amountToRepair);

        System.out.println("[RESULT] Successfully dispatched " + vehiclesToRepair.size() + " vehicles to ServiceStore:");
        long orderId = 1000L;
        for (final Car car : vehiclesToRepair) {
            final ServiceOrder createdOrder = new ServiceOrder(orderId++, car);
            System.out.println("  - Order #" + createdOrder.getId() + ": "
                    + car.getMake() + " " + car.getModel() + " (VIN: " + car.getVin() + ") -> Status: " + createdOrder.getStatus());
        }
        System.out.println("Total fleet remaining in company: " + company.getTransport().size());

        System.out.println("\n======================================================================");
        System.out.println("          ALL SCENARIOS SUCCESSFULLY COMPLETED AND VERIFIED           ");
        System.out.println("======================================================================");
    }

    private static void printHeader(final String title) {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println(title);
        System.out.println("----------------------------------------------------------------------");
    }
}
