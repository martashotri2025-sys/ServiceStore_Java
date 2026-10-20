package carService.service;

import carService.domain.Mechanic;
import carService.domain.OrderStatus;
import carService.domain.RepairOrder;
import carService.domain.ServiceWork;
import carService.price.PriceCalculator;
import carService.repository.RepairOrderRepository;

import java.math.BigDecimal;

public class CarService {

    private final RepairOrderRepository repository;
    private final PriceCalculator priceCalculator;

    public CarService(RepairOrderRepository repository, PriceCalculator priceCalculator) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository dependency is required.");
        }
        if (priceCalculator == null) {
            throw new IllegalArgumentException("Price calculator dependency is required.");
        }
        this.repository = repository;
        this.priceCalculator = priceCalculator;
    }

    public void registerRepairOrder(RepairOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        repository.saveOrder(order);
    }

    public RepairOrder retrieveOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null.");
        }
        return repository.findOrderById(orderId)
                .orElseThrow(() -> new CarServiceException("Repair order with ID " + orderId + " does not exist."));
    }

    public void diagnoseOrder(Long orderId) {
        RepairOrder order = retrieveOrder(orderId);
        order.performDiagnosis();
        repository.saveOrder(order);
    }

    public void addWorkToOrder(Long orderId, ServiceWork work) {
        RepairOrder order = retrieveOrder(orderId);
        order.attachWork(work);
        repository.saveOrder(order);
    }

    public void approveOrder(Long orderId) {
        RepairOrder order = retrieveOrder(orderId);
        order.approveByClient();
        repository.saveOrder(order);
    }

    public void assignMechanicToOrder(Long orderId, Mechanic mechanic) {
        RepairOrder order = retrieveOrder(orderId);
        order.delegateToMechanic(mechanic);
        repository.saveOrder(order);
    }

    public void startOrderRepair(Long orderId) {
        RepairOrder order = retrieveOrder(orderId);
        order.startRepairProcess();
        repository.saveOrder(order);
    }

    public void finishOrderRepair(Long orderId) {
        RepairOrder order = retrieveOrder(orderId);
        order.finishRepairProcess();
        repository.saveOrder(order);
    }

    public BigDecimal calculateInvoiceAmount(Long orderId) {
        RepairOrder order = retrieveOrder(orderId);

        if (order.getCurrentStatus() != OrderStatus.SUCCESSFULLY_COMPLETED) {
            throw new IllegalStateException("Invoice can only be calculated for successfully completed orders.");
        }

        BigDecimal baseTotal = order.calculateGrandTotal();
        return priceCalculator.calculateCommercialPrice(baseTotal);
    }

    public BigDecimal processCommercialInvoice(Long orderId) {
        return calculateInvoiceAmount(orderId);
    }
}