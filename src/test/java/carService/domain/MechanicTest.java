package carService.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Mechanic Domain Model Unit Tests")
class MechanicTest {

    private static final String VALID_NAME = "Bogdan Melnyk";

    @Test
    @DisplayName("Successfully creates mechanic with valid name and initial available state")
    void createMechanic_WithValidData_ShouldInitializeCorrectly() {
        final String name = VALID_NAME;
        final Mechanic mechanic = new Mechanic(name);

        assertAll("Mechanic properties check",
                () -> assertNotNull(mechanic.getId(), "Mechanic ID should not be null"),
                () -> assertEquals(VALID_NAME, mechanic.getFullName(), "Mechanic full name should match"),
                () -> assertTrue(mechanic.isAvailable(), "New mechanic should initially be available")
        );
    }

    @Test
    @DisplayName("Trims whitespace from mechanic full name")
    void createMechanic_WithWhitespaceInName_ShouldTrimName() {
        final String untrimmedName = "   " + VALID_NAME + " \t\n";
        final Mechanic mechanic = new Mechanic(untrimmedName);

        assertEquals(VALID_NAME, mechanic.getFullName(), "Name should be properly trimmed");
    }

    @ParameterizedTest(name = "Invalid mechanic name: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Throws IllegalArgumentException when mechanic name is null or blank")
    void createMechanic_WithInvalidName_ShouldThrowException(final String invalidName) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Mechanic(invalidName),
                "Should reject blank or null mechanic name"
        );
        assertEquals("Mechanic full name cannot be empty.", exception.getMessage());
    }

    @Test
    @DisplayName("assignToOrder marks available mechanic as unavailable")
    void assignToOrder_WhenMechanicIsAvailable_ShouldSetAvailableToFalse() {
        final Mechanic mechanic = new Mechanic(VALID_NAME);
        mechanic.assignToOrder();
        assertFalse(mechanic.isAvailable(), "Mechanic should no longer be available after assignment");
    }

    @Test
    @DisplayName("assignToOrder throws IllegalStateException when mechanic is already busy")
    void assignToOrder_WhenMechanicIsAlreadyBusy_ShouldThrowException() {
        final Mechanic mechanic = new Mechanic(VALID_NAME);
        mechanic.assignToOrder();
        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                mechanic::assignToOrder,
                "Should not allow assigning already busy mechanic"
        );
        assertTrue(exception.getMessage().contains("is currently busy"),
                "Exception message should indicate mechanic is busy");
    }

    @Test
    @DisplayName("releaseFromOrder marks busy mechanic as available")
    void releaseFromOrder_WhenMechanicIsBusy_ShouldSetAvailableToTrue() {
        final Mechanic mechanic = new Mechanic(VALID_NAME);
        mechanic.assignToOrder();
        mechanic.releaseFromOrder();
        assertTrue(mechanic.isAvailable(), "Mechanic should be available after release");
    }

    @Test
    @DisplayName("releaseFromOrder throws IllegalStateException when mechanic is already available")
    void releaseFromOrder_WhenMechanicIsAlreadyAvailable_ShouldThrowException() {
        final Mechanic mechanic = new Mechanic(VALID_NAME);

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                mechanic::releaseFromOrder,
                "Should not allow releasing an already available mechanic"
        );
        assertTrue(exception.getMessage().contains("is already available"),
                "Exception message should indicate mechanic is already available");
    }

    @Test
    @DisplayName("Verify equals and hashCode contract based on mechanic ID")
    void equalsAndHashCode_BasedOnId() {
        final Mechanic firstMechanic = new Mechanic(VALID_NAME);
        final Mechanic secondMechanic = new Mechanic(VALID_NAME);

        assertEquals(firstMechanic, firstMechanic, "Mechanic should equal itself");
        assertNotEquals(firstMechanic, secondMechanic, "Different instances with unique IDs should not be equal");
        assertFalse(firstMechanic.equals(null), "Mechanic should not equal null");
        assertFalse(firstMechanic.equals(new Object()), "Mechanic should not equal different class instance");
    }

    @Test
    @DisplayName("toString contains mechanic details")
    void toString_ShouldContainMechanicDetails() {
        final Mechanic mechanic = new Mechanic(VALID_NAME);
        final String mechanicString = mechanic.toString();

        assertNotNull(mechanicString);
        assertTrue(mechanicString.contains("Mechanic"));
        assertTrue(mechanicString.contains(VALID_NAME));
    }
}
