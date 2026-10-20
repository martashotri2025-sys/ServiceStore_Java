package carService.repository;

import carService.domain.RepairOrder;
import java.util.List;
import java.util.Optional;

public interface RepairOrderRepository {
    void saveOrder(RepairOrder order);
    Optional<RepairOrder> findOrderById(Long id);
    List<RepairOrder> fetchAllOrders();
}