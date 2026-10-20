package carService.price;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DiscountRule Pricing Rule Unit Tests")
class DiscountRuleTest {

    @ParameterizedTest(name = "Base price: {0}, rate: {1} => expected: {2}")
    @CsvSource({
            "1000.00, 0.10, 900.00",
            "500.00, 0.20, 400.00",
            "100.00, 0.00, 100.00",
            "100.00, 1.00, 0.00",
            "33.33, 0.15, 28.33"
    })
    @DisplayName("Calculates discounted price correctly")
    void applyRule_WithValidData_ShouldCalculateDiscountedPrice(
            String basePriceStr,
            String rateStr,
            String expectedStr
    ) {
        DiscountRule rule = new DiscountRule(rateStr);

        BigDecimal result = rule.applyRule(new BigDecimal(basePriceStr));

        assertEquals(new BigDecimal(expectedStr), result);
    }

    @Test
    @DisplayName("Calculates discounted price using BigDecimal constructor")
    void applyRule_WithBigDecimalConstructor_ShouldCalculateDiscountedPrice() {
        DiscountRule rule = new DiscountRule(new BigDecimal("0.25"));

        BigDecimal result = rule.applyRule(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    @DisplayName("Accepts whitespace around discount rate")
    void constructor_WithWhitespace_ShouldAcceptRate() {
        DiscountRule rule = new DiscountRule("  0.10  ");

        BigDecimal result = rule.applyRule(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("90.00"), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1.0", "1.01", "2.0"})
    @DisplayName("Rejects discount rates outside the allowed range")
    void constructor_WithInvalidRate_ShouldThrowException(String rate) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new DiscountRule(rate)
        );

        assertEquals(
                "Discount rate must be between 0.0 and 1.0.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Rejects null String discount rate")
    void constructor_WithNullString_ShouldThrowException() {
        assertThrows(
                NullPointerException.class,
                () -> new DiscountRule((String) null)
        );
    }

    @Test
    @DisplayName("Rejects null BigDecimal discount rate")
    void constructor_WithNullBigDecimal_ShouldThrowException() {
        assertThrows(
                NullPointerException.class,
                () -> new DiscountRule((BigDecimal) null)
        );
    }

    @Test
    @DisplayName("Rejects non-numeric discount rate")
    void constructor_WithInvalidString_ShouldThrowException() {
        assertThrows(
                NumberFormatException.class,
                () -> new DiscountRule("invalid")
        );
    }

    @Test
    @DisplayName("Rejects null base price")
    void applyRule_WithNullBasePrice_ShouldThrowException() {
        DiscountRule rule = new DiscountRule("0.10");

        assertThrows(
                NullPointerException.class,
                () -> rule.applyRule(null)
        );
    }

    @Test
    @DisplayName("Rejects negative base price")
    void applyRule_WithNegativeBasePrice_ShouldThrowException() {
        DiscountRule rule = new DiscountRule("0.10");

        assertThrows(
                IllegalArgumentException.class,
                () -> rule.applyRule(new BigDecimal("-100.00"))
        );
    }

    @Test
    @DisplayName("Returns zero for zero base price")
    void applyRule_WithZeroBasePrice_ShouldReturnZero() {
        DiscountRule rule = new DiscountRule("0.10");

        BigDecimal result = rule.applyRule(BigDecimal.ZERO);

        assertEquals(new BigDecimal("0.00"), result);
    }
}