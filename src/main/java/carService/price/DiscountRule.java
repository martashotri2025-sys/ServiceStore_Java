package carService.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class DiscountRule implements PricingRule {

    private final BigDecimal discountMultiplier;

    public DiscountRule(String discountRate) {
        this(new BigDecimal(
                Objects.requireNonNull(discountRate, "Discount rate must be provided.").trim()));
    }

    public DiscountRule(BigDecimal discountRate) {
        Objects.requireNonNull( discountRate, "Discount rate must be provided.");

        if (discountRate.compareTo(BigDecimal.ZERO) < 0
                || discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Discount rate must be between 0.0 and 1.0."
            );
        }
        this.discountMultiplier = BigDecimal.ONE.subtract(discountRate);
    }

    @Override
    public BigDecimal applyRule(BigDecimal basePrice) {
        Objects.requireNonNull(basePrice, "Base price cannot be null."
        );

        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price cannot be negative.");
        }

        return basePrice
                .multiply(discountMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }
}