package carService.price;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriceCalculator Unit Tests")
class PriceCalculatorTest {

    @Test
    @DisplayName("Throws exception when rules list is null")
    void constructor_WithNullRulesList_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PriceCalculator(null)
        );

        assertEquals(
                "Pricing rules list cannot be null.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Calculates price with empty rules list and rounds the result")
    void calculateCommercialPrice_WithEmptyRules_ShouldReturnRoundedPrice() {
        PriceCalculator calculator = new PriceCalculator(Collections.emptyList());

        BigDecimal result = calculator.calculateCommercialPrice(
                new BigDecimal("250.555")
        );

        assertEquals(new BigDecimal("250.56"), result);
    }

    @Test
    @DisplayName("Returns price with two decimal places when no rounding is needed")
    void calculateCommercialPrice_WithWholePrice_ShouldReturnPriceWithTwoDecimalPlaces() {
        PriceCalculator calculator = new PriceCalculator(Collections.emptyList());

        BigDecimal result = calculator.calculateCommercialPrice(
                new BigDecimal("250")
        );

        assertEquals(new BigDecimal("250.00"), result);
    }

    @Test
    @DisplayName("Applies multiple rules sequentially")
    void calculateCommercialPrice_WithMockedRules_ShouldApplySequentially() {
        PricingRule firstRule = mock(PricingRule.class);
        PricingRule secondRule = mock(PricingRule.class);

        BigDecimal initialPrice = new BigDecimal("100.00");
        BigDecimal intermediatePrice = new BigDecimal("80.00");
        BigDecimal expectedFinalPrice = new BigDecimal("95.00");

        when(firstRule.applyRule(initialPrice)).thenReturn(intermediatePrice);
        when(secondRule.applyRule(intermediatePrice)).thenReturn(expectedFinalPrice);

        List<PricingRule> rules = List.of(firstRule, secondRule);
        PriceCalculator calculator = new PriceCalculator(rules);

        BigDecimal result = calculator.calculateCommercialPrice(initialPrice);

        assertEquals(expectedFinalPrice, result);

        InOrder inOrder = inOrder(firstRule, secondRule);
        inOrder.verify(firstRule).applyRule(initialPrice);
        inOrder.verify(secondRule).applyRule(intermediatePrice);
    }

    @Test
    @DisplayName("Combines discount and minimum cost rules")
    void calculateCommercialPrice_WithRealDiscountAndMinimumRules_ShouldApplyBoth() {
        List<PricingRule> rules = List.of(
                new DiscountRule("0.10"),
                new MinimumCostRule("500.00")
        );

        PriceCalculator calculator = new PriceCalculator(rules);

        BigDecimal resultA = calculator.calculateCommercialPrice(
                new BigDecimal("1000.00")
        );

        BigDecimal resultB = calculator.calculateCommercialPrice(
                new BigDecimal("400.00")
        );

        assertEquals(new BigDecimal("900.00"), resultA);
        assertEquals(new BigDecimal("500.00"), resultB);
    }

    @Test
    @DisplayName("Applies minimum cost before discount when rules are ordered that way")
    void calculateCommercialPrice_WithMinimumRuleBeforeDiscount_ShouldRespectRuleOrder() {
        List<PricingRule> rules = List.of(
                new MinimumCostRule("500.00"),
                new DiscountRule("0.10")
        );

        PriceCalculator calculator = new PriceCalculator(rules);

        BigDecimal result = calculator.calculateCommercialPrice(
                new BigDecimal("400.00")
        );

        assertEquals(new BigDecimal("450.00"), result);
    }

    @Test
    @DisplayName("Rejects null initial price")
    void calculateCommercialPrice_WithNullPrice_ShouldThrowException() {
        PriceCalculator calculator = new PriceCalculator(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateCommercialPrice(null)
        );

        assertEquals(
                "Initial price must be a non-negative value.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects negative initial price")
    void calculateCommercialPrice_WithNegativePrice_ShouldThrowException() {
        PriceCalculator calculator = new PriceCalculator(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateCommercialPrice(
                        new BigDecimal("-10.00")
                )
        );

        assertEquals(
                "Initial price must be a non-negative value.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Does not execute rules when initial price is invalid")
    void calculateCommercialPrice_WithInvalidPrice_ShouldNotExecuteRules() {
        PricingRule rule = mock(PricingRule.class);
        PriceCalculator calculator = new PriceCalculator(List.of(rule));

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateCommercialPrice(new BigDecimal("-10.00"))
        );

        verifyNoInteractions(rule);
    }

    @Test
    @DisplayName("Copies the rules list provided to the constructor")
    void constructor_WhenOriginalListChanges_ShouldKeepOriginalRules() {
        PricingRule rule = mock(PricingRule.class);
        List<PricingRule> rules = new java.util.ArrayList<>();
        rules.add(rule);

        PriceCalculator calculator = new PriceCalculator(rules);

        rules.clear();

        BigDecimal initialPrice = new BigDecimal("100.00");
        BigDecimal expectedPrice = new BigDecimal("90.00");

        when(rule.applyRule(initialPrice)).thenReturn(expectedPrice);

        BigDecimal result = calculator.calculateCommercialPrice(initialPrice);

        assertEquals(expectedPrice, result);
    }
}