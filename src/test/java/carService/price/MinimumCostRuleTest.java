package carService.price;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MinimumCostRule Pricing Rule Unit Tests")
class MinimumCostRuleTest {

    @ParameterizedTest(name = "Base price: {0}, threshold: {1} => expected: {2}")
    @CsvSource({
            "300.00, 500.00, 500.00",
            "500.00, 500.00, 500.00",
            "750.00, 500.00, 750.00",
            "0.00, 100.00, 100.00",
            "100.00, 0.00, 100.00"
    })
    @DisplayName("Applies minimum cost rule correctly")
    void applyRule_WithValidData_ShouldEnforceMinimumThreshold(
            String basePriceStr,
            String thresholdStr,
            String expectedStr
    ) {
        MinimumCostRule rule = new MinimumCostRule(thresholdStr);

        BigDecimal result = rule.applyRule(new BigDecimal(basePriceStr));

        assertEquals(new BigDecimal(expectedStr), result);
    }

    @Test
    @DisplayName("Applies minimum cost rule using BigDecimal constructor")
    void applyRule_WithBigDecimalConstructor_ShouldEnforceMinimumThreshold() {
        BigDecimal threshold = new BigDecimal("450.00");
        MinimumCostRule rule = new MinimumCostRule(threshold);

        assertEquals(
                threshold,
                rule.applyRule(new BigDecimal("200.00"))
        );

        assertEquals(
                new BigDecimal("600.00"),
                rule.applyRule(new BigDecimal("600.00"))
        );
    }

    @Test
    @DisplayName("Accepts whitespace around minimum cost")
    void constructor_WithWhitespace_ShouldAcceptValue() {
        MinimumCostRule rule = new MinimumCostRule("  500.00  ");

        BigDecimal result = rule.applyRule(new BigDecimal("300.00"));

        assertEquals(new BigDecimal("500.00"), result);
    }

    @Test
    @DisplayName("Rejects null String threshold")
    void constructor_WithNullString_ShouldThrowException() {
        assertThrows(
                NullPointerException.class,
                () -> new MinimumCostRule((String) null)
        );
    }

    @Test
    @DisplayName("Rejects null BigDecimal threshold")
    void constructor_WithNullBigDecimal_ShouldThrowException() {
        assertThrows(
                NullPointerException.class,
                () -> new MinimumCostRule((BigDecimal) null)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "   "})
    @DisplayName("Rejects non-numeric threshold")
    void constructor_WithInvalidString_ShouldThrowException(String threshold) {
        assertThrows(
                NumberFormatException.class,
                () -> new MinimumCostRule(threshold)
        );
    }

    @Test
    @DisplayName("Rejects negative minimum cost")
    void constructor_WithNegativeThreshold_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MinimumCostRule(new BigDecimal("-100.00"))
        );

        assertEquals(
                "Minimum cost cannot be negative.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects null base price")
    void applyRule_WithNullBasePrice_ShouldThrowException() {
        MinimumCostRule rule = new MinimumCostRule("500.00");

        assertThrows(
                NullPointerException.class,
                () -> rule.applyRule(null)
        );
    }

    @Test
    @DisplayName("Rejects negative base price")
    void applyRule_WithNegativeBasePrice_ShouldThrowException() {
        MinimumCostRule rule = new MinimumCostRule("500.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> rule.applyRule(new BigDecimal("-200.00"))
        );
    }
}