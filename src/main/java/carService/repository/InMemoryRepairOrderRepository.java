package carService.repository;

import carService.domain.RepairOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepairOrderRepository implements RepairOrderRepository {

    private final Map<Long, RepairOrder> database = new HashMap<>();

    @Override
    public void saveOrder(RepairOrder order) {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Cannot save a null order or an order without an ID.");
        }
        database.put(order.getOrderId(), order);
    }

    @Override
    public Optional<RepairOrder> findOrderById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<RepairOrder> fetchAllOrders() {
        return new ArrayList<>(database.values());
    }
}