import java.math.BigDecimal;
import java.util.Objects;

/**
 * Individual service work in an order.
 * Encapsulates parts cost, labor cost, and completion status.
 * Validation is performed in respective setters using trim().
 */
public class ServiceWork {

    private String description;
    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private Boolean completed;

    public ServiceWork() {
        this.completed = false;
    }

    public ServiceWork(
            final String description,
            final BigDecimal partsCost,
            final BigDecimal laborCost
    ) {
        setDescription(description);
        setPartsCost(partsCost);
        setLaborCost(laborCost);
        this.completed = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        if (description == null || description.trim().isBlank()) {
            throw new IllegalArgumentException("Work description cannot be empty.");
        }
        this.description = description.trim();
    }

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(final BigDecimal partsCost) {
        if (partsCost == null || partsCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Parts cost cannot be negative or null.");
        }
        this.partsCost = partsCost;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(final BigDecimal laborCost) {
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Labor cost cannot be negative or null.");
        }
        this.laborCost = laborCost;
    }

    public BigDecimal getTotalCost() {
        return (partsCost != null ? partsCost : BigDecimal.ZERO)
                .add(laborCost != null ? laborCost : BigDecimal.ZERO);
    }

    public Boolean isCompleted() {
        return completed;
    }


    public void markAsCompleted() {
        if (Boolean.TRUE.equals(this.completed)) {
            throw new CarServiceException("Work '" + description + "' is already completed.");
        }
        this.completed = true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ServiceWork that = (ServiceWork) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(description);
    }

    @Override
    public String toString() {
        return "ServiceWork{" +
                "'" + description + '\'' +
                ", partsCost=" + partsCost +
                ", laborCost=" + laborCost +
                ", totalCost=" + getTotalCost() +
                ", completed=" + completed +
                '}';
    }
}
