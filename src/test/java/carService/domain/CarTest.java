package carService.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Car Domain Model Unit Tests")
class CarTest {

    private static final String VALID_VIN = "1HGCR2F83HA123456";
    private static final String VALID_MAKE = "Honda";
    private static final String VALID_MODEL = "Accord";
    private static final int VALID_YEAR = 2022;

    private Client defaultOwner;

    @BeforeEach
    void setUp() {
        this.defaultOwner = new Client("Alexander", "Shevchenko");
    }

    @Test
    @DisplayName("Successfully creates car with valid attributes and trims inputs")
    void createCar_WithValidData_ShouldInitializeCorrectly() {
        final String untrimmedVin = "  " + VALID_VIN + "  ";
        final String untrimmedMake = "  " + VALID_MAKE + " ";
        final String untrimmedModel = " " + VALID_MODEL + "  ";

        final Car car = new Car(untrimmedVin, untrimmedMake, untrimmedModel, VALID_YEAR, defaultOwner);

        assertAll("Car fields verification",
                () -> assertEquals(VALID_VIN, car.getVin(), "VIN should be trimmed and match"),
                () -> assertEquals(VALID_MAKE, car.getMake(), "Make should be trimmed and match"),
                () -> assertEquals(VALID_MODEL, car.getModel(), "Model should be trimmed and match"),
                () -> assertEquals(VALID_YEAR, car.getProductionYear(), "Production year should match"),
                () -> assertSame(defaultOwner, car.getOwner(), "Owner should match the assigned client")
        );
    }

    @Test
    @DisplayName("Successfully changes car owner")
    void changeOwner_WithValidNewOwner_ShouldUpdateOwner() {
        final Car car = new Car(VALID_VIN, VALID_MAKE, VALID_MODEL, VALID_YEAR, defaultOwner);
        final Client newOwner = new Client("Maria", "Kovalenko");

        car.changeOwner(newOwner);

        assertSame(newOwner, car.getOwner(), "Owner should be updated to new client");
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when changing owner to null")
    void changeOwner_WithNullOwner_ShouldThrowException() {
        final Car car = new Car(VALID_VIN, VALID_MAKE, VALID_MODEL, VALID_YEAR, defaultOwner);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> car.changeOwner(null),
                "Should reject null owner in changeOwner"
        );
        assertEquals("Car must have a valid owner.", exception.getMessage());
    }

    @ParameterizedTest(name = "Invalid VIN: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Throws IllegalArgumentException when VIN is null or blank")
    void createCar_WithInvalidVin_ShouldThrowException(final String invalidVin) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Car(invalidVin, VALID_MAKE, VALID_MODEL, VALID_YEAR, defaultOwner),
                "Should reject blank or null VIN"
        );
        assertEquals("Vehicle Identification Number cannot be blank.", exception.getMessage());
    }

    @ParameterizedTest(name = "Invalid production year: {0}")
    @ValueSource(ints = {1800, 1885, 2999})
    @DisplayName("Throws IllegalArgumentException when production year is out of valid bounds")
    void createCar_WithOutOfBoundsYear_ShouldThrowException(final int invalidYear) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Car(VALID_VIN, VALID_MAKE, VALID_MODEL, invalidYear, defaultOwner),
                "Should reject out-of-bounds production year"
        );
        assertTrue(exception.getMessage().contains("out of valid bounds"),
                "Exception message should mention out of valid bounds");
    }

    @Test
    @DisplayName("Boundary production years (1886 and current year + 1) should be accepted")
    void createCar_WithBoundaryProductionYears_ShouldSucceed() {
        final int minAllowedYear = 1886;
        final int maxAllowedYear = Year.now().getValue() + 1;

        final Car oldestCar = new Car("VIN-OLDEST", VALID_MAKE, VALID_MODEL, minAllowedYear, defaultOwner);
        final Car newestCar = new Car("VIN-NEWEST", VALID_MAKE, VALID_MODEL, maxAllowedYear, defaultOwner);

        assertEquals(minAllowedYear, oldestCar.getProductionYear(), "Minimum year 1886 should be accepted");
        assertEquals(maxAllowedYear, newestCar.getProductionYear(), "Max year (now + 1) should be accepted");
    }

    @ParameterizedTest(name = "Invalid make: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Throws IllegalArgumentException when car make is null or blank")
    void createCar_WithInvalidMake_ShouldThrowException(final String invalidMake) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Car(VALID_VIN, invalidMake, VALID_MODEL, VALID_YEAR, defaultOwner),
                "Should reject blank or null make"
        );
        assertEquals("Car make is mandatory.", exception.getMessage());
    }

    @ParameterizedTest(name = "Invalid model: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Throws IllegalArgumentException when car model is null or blank")
    void createCar_WithInvalidModel_ShouldThrowException(final String invalidModel) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Car(VALID_VIN, VALID_MAKE, invalidModel, VALID_YEAR, defaultOwner),
                "Should reject blank or null model"
        );
        assertEquals("Car model is mandatory.", exception.getMessage());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when initial owner is null")
    void createCar_WithNullOwner_ShouldThrowException() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Car(VALID_VIN, VALID_MAKE, VALID_MODEL, VALID_YEAR, null),
                "Should reject null initial owner"
        );
        assertEquals("Car must have a valid owner.", exception.getMessage());
    }

    @Test
    @DisplayName("Verifies equals and hashCode based on VIN")
    void equalsAndHashCode_BasedOnVin() {
        final Client anotherOwner = new Client("Dmytro", "Bondar");
        final Car firstCar = new Car(VALID_VIN, VALID_MAKE, VALID_MODEL, VALID_YEAR, defaultOwner);
        final Car sameVinCar = new Car(VALID_VIN, "OtherMake", "OtherModel", 2020, anotherOwner);
        final Car differentVinCar = new Car("DIFFERENT_VIN_123", VALID_MAKE, VALID_MODEL, VALID_YEAR, defaultOwner);

        assertEquals(firstCar, sameVinCar, "Cars with same VIN should be equal");
        assertEquals(firstCar.hashCode(), sameVinCar.hashCode(), "Cars with same VIN should have equal hashCodes");
        assertNotEquals(firstCar, differentVinCar, "Cars with different VINs should not be equal");
        assertFalse(firstCar.equals(null), "Car should not equal null");
    }

    @Test
    @DisplayName("toString includes car details")
    void toString_ShouldContainCarDetails() {
        final Car car = new Car(VALID_VIN, VALID_MAKE, VALID_MODEL, VALID_YEAR, defaultOwner);

        final String carString = car.toString();

        assertNotNull(carString);
        assertTrue(carString.contains(VALID_VIN), "toString should contain VIN");
        assertTrue(carString.contains(VALID_MAKE), "toString should contain make");
        assertTrue(carString.contains(VALID_MODEL), "toString should contain model");
    }

    @ParameterizedTest(name = "Specialization parse test for: {0}")
    @ValueSource(strings = {"ENGINE_REPAIR", "engine_repair", "  ELECTRICAL  ", "Diagnostics"})
    @DisplayName("MechanicSpecialization.fromValue parses valid specializations ignoring case and whitespace")
    void mechanicSpecialization_FromValue_WithValidString_ShouldReturnEnum(final String validValue) {
        final Car.MechanicSpecialization specialization = Car.MechanicSpecialization.fromValue(validValue);

        assertNotNull(specialization, "Parsed specialization should not be null");
    }

    @ParameterizedTest(name = "Invalid specialization value: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("MechanicSpecialization.fromValue throws exception for null or blank input")
    void mechanicSpecialization_FromValue_WithNullOrBlank_ShouldThrowException(final String invalidValue) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Car.MechanicSpecialization.fromValue(invalidValue)
        );
        assertEquals("Specialization value cannot be null or blank.", exception.getMessage());
    }

    @Test
    @DisplayName("MechanicSpecialization.fromValue throws exception for unknown value")
    void mechanicSpecialization_FromValue_WithUnknownValue_ShouldThrowException() {
        final String unknownValue = "ROCKET_SCIENCE";

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Car.MechanicSpecialization.fromValue(unknownValue)
        );
        assertTrue(exception.getMessage().contains("Unknown mechanic specialization"),
                "Exception message should mention unknown specialization");
    }
}

