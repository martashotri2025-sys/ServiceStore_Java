package carService.price;

import java.math.BigDecimal;
import java.util.Objects;

public class MinimumCostRule implements PricingRule {

    private final BigDecimal minimumThreshold;

    public MinimumCostRule(String minimumCost) {
        this(new BigDecimal(Objects.requireNonNull(minimumCost, "Minimum cost threshold must be provided.").trim()));
    }

    public MinimumCostRule(BigDecimal minimumCost) {
        Objects.requireNonNull(minimumCost, "Minimum cost threshold must be provided.");

        if (minimumCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum cost cannot be negative.");
        }

        this.minimumThreshold = minimumCost;
    }

    @Override
    public BigDecimal applyRule(BigDecimal basePrice) {
        Objects.requireNonNull(basePrice, "Base price cannot be null.");

        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price cannot be negative.");
        }
        return basePrice.compareTo(minimumThreshold) < 0 ? minimumThreshold : basePrice;
    }
}