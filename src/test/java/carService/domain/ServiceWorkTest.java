package carService.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ServiceWork Domain Model Unit Tests")
class ServiceWorkTest {

    private static final String VALID_TITLE = "Brake pad replacement";
    private static final BigDecimal VALID_PARTS_COST = new BigDecimal("850.00");
    private static final BigDecimal VALID_LABOR_COST = new BigDecimal("400.00");

    @Test
    @DisplayName("Successfully creates ServiceWork with valid values and initial unfinished state")
    void createServiceWork_WithValidData_ShouldInitializeCorrectly() {
        final String title = VALID_TITLE;
        final BigDecimal partsCost = VALID_PARTS_COST;
        final BigDecimal laborCost = VALID_LABOR_COST;

        final ServiceWork work = new ServiceWork(title, partsCost, laborCost);

        assertAll("ServiceWork properties check",
                () -> assertNotNull(work.getId(), "ServiceWork ID should not be null"),
                () -> assertEquals(VALID_TITLE, work.getTitle(), "Title should match"),
                () -> assertEquals(VALID_PARTS_COST, work.getPartsCost(), "Parts cost should match"),
                () -> assertEquals(VALID_LABOR_COST, work.getLaborCost(), "Labor cost should match"),
                () -> assertFalse(work.isFinished(), "ServiceWork should initially be unfinished")
        );
    }

    @Test
    @DisplayName("Trims whitespace from title")
    void createServiceWork_WithWhitespaceTitle_ShouldTrimTitle() {
        final String untrimmedTitle = "  " + VALID_TITLE + "  \t";

        final ServiceWork work = new ServiceWork(untrimmedTitle, VALID_PARTS_COST, VALID_LABOR_COST);

        assertEquals(VALID_TITLE, work.getTitle(), "Title should be trimmed");
    }

    @ParameterizedTest(name = "Invalid title: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Throws IllegalArgumentException when title is null or blank")
    void createServiceWork_WithInvalidTitle_ShouldThrowException(final String invalidTitle) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceWork(invalidTitle, VALID_PARTS_COST, VALID_LABOR_COST),
                "Should reject blank or null title"
        );
        assertEquals("Service work title is missing.", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when parts cost is null")
    void createServiceWork_WithNullPartsCost_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceWork(VALID_TITLE, null, VALID_LABOR_COST)
        );
        assertEquals("Parts cost cannot be negative or null.", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when parts cost is negative")
    void createServiceWork_WithNegativePartsCost_ShouldThrowException() {
        final BigDecimal negativePartsCost = new BigDecimal("-1.00");

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceWork(VALID_TITLE, negativePartsCost, VALID_LABOR_COST)
        );
        assertEquals("Parts cost cannot be negative or null.", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when labor cost is null")
    void createServiceWork_WithNullLaborCost_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceWork(VALID_TITLE, VALID_PARTS_COST, null)
        );
        assertEquals("Labor cost cannot be negative or null.", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when labor cost is negative")
    void createServiceWork_WithNegativeLaborCost_ShouldThrowException() {
        final BigDecimal negativeLaborCost = new BigDecimal("-50.00");

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceWork(VALID_TITLE, VALID_PARTS_COST, negativeLaborCost)
        );
        assertEquals("Labor cost cannot be negative or null.", exception.getMessage());
    }

    @Test
    @DisplayName("Allows zero costs for free labor or parts")
    void createServiceWork_WithZeroCosts_ShouldSucceed() {
        final BigDecimal zero = BigDecimal.ZERO;

        final ServiceWork work = new ServiceWork(VALID_TITLE, zero, zero);

        assertEquals(zero, work.getPartsCost());
        assertEquals(zero, work.getLaborCost());
        assertEquals(BigDecimal.ZERO, work.getTotalCost());
    }

    @Test
    @DisplayName("markAsFinished changes finished state to true")
    void markAsFinished_ShouldSetFinishedToTrue() {
        final ServiceWork work = new ServiceWork(VALID_TITLE, VALID_PARTS_COST, VALID_LABOR_COST);

        work.markAsFinished();

        assertTrue(work.isFinished(), "Work should be finished after markAsFinished");
    }

    @Test
    @DisplayName("getTotalCost returns exact sum of partsCost and laborCost using BigDecimal")
    void getTotalCost_ShouldCalculateSumOfPartsAndLabor() {
        final BigDecimal parts = new BigDecimal("850.50");
        final BigDecimal labor = new BigDecimal("400.25");
        final BigDecimal expectedTotal = new BigDecimal("1250.75");
        final ServiceWork work = new ServiceWork(VALID_TITLE, parts, labor);

        final BigDecimal totalCost = work.getTotalCost();

        assertEquals(expectedTotal, totalCost, "Total cost should equal sum of parts and labor");
    }

    @Test
    @DisplayName("Verify equals and hashCode based on UUID")
    void equalsAndHashCode_BasedOnId() {
        final ServiceWork firstWork = new ServiceWork(VALID_TITLE, VALID_PARTS_COST, VALID_LABOR_COST);
        final ServiceWork secondWork = new ServiceWork(VALID_TITLE, VALID_PARTS_COST, VALID_LABOR_COST);

        assertEquals(firstWork, firstWork, "ServiceWork should equal itself");
        assertNotEquals(firstWork, secondWork, "Different instances should not be equal");
        assertFalse(firstWork.equals(null), "Should not equal null");
    }

    @Test
    @DisplayName("toString contains ServiceWork details")
    void toString_ShouldContainWorkDetails() {
        final ServiceWork work = new ServiceWork(VALID_TITLE, VALID_PARTS_COST, VALID_LABOR_COST);

        final String str = work.toString();

        assertNotNull(str);
        assertTrue(str.contains("ServiceWork"));
        assertTrue(str.contains(VALID_TITLE));
    }
}

