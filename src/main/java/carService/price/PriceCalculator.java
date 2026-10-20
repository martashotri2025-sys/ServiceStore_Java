package carService.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PriceCalculator {

    private final List<PricingRule> activeRules;

    public PriceCalculator(List<PricingRule> activeRules) {
        if (activeRules == null) {
            throw new IllegalArgumentException("Pricing rules list cannot be null.");
        }
        this.activeRules = List.copyOf(activeRules);
    }

    public BigDecimal calculateCommercialPrice(BigDecimal initialPrice) {
        if (initialPrice == null || initialPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial price must be a non-negative value.");
        }

        BigDecimal finalCalculatedPrice = initialPrice;

        for (PricingRule rule : activeRules) {
            finalCalculatedPrice = rule.applyRule(finalCalculatedPrice);
        }

        return finalCalculatedPrice.setScale(2, RoundingMode.HALF_UP);
    }
}