package carService.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class ServiceWork {

    private final UUID id;
    private final String title;
    private final BigDecimal partsCost;
    private final BigDecimal laborCost;
    private boolean finished;

    public ServiceWork(String title, BigDecimal partsCost, BigDecimal laborCost) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Service work title is missing."
            );
        }

        if (partsCost == null || partsCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Parts cost cannot be negative or null."
            );
        }

        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Labor cost cannot be negative or null."
            );
        }

        this.id = UUID.randomUUID();
        this.title = title.trim();
        this.partsCost = partsCost;
        this.laborCost = laborCost;
        this.finished = false;
    }

    public void markAsFinished() {
        this.finished = true;
    }

    public BigDecimal getTotalCost() {
        return partsCost.add(laborCost);
    }
}