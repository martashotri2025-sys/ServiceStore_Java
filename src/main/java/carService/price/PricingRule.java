package carService.price;

import java.math.BigDecimal;

public interface PricingRule {
    BigDecimal applyRule(BigDecimal basePrice);
}