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

@DisplayName("Client Domain Model Unit Tests")
class ClientTest {

    private static final String VALID_FIRST_NAME = "Alexander";
    private static final String VALID_LAST_NAME = "Shevchenko";

    @Test
    @DisplayName("Successfully creates client with valid first and last names")
    void createClient_WithValidData_ShouldInitializeCorrectly() {
        final String firstName = VALID_FIRST_NAME;
        final String lastName = VALID_LAST_NAME;
        final Client client = new Client(firstName, lastName);
        assertAll("Client properties validation",
                () -> assertNotNull(client.getId(), "Client ID should not be null"),
                () -> assertEquals(VALID_FIRST_NAME, client.getFirstName(), "First name should match"),
                () -> assertEquals(VALID_LAST_NAME, client.getLastName(), "Last name should match")
        );
    }

    @Test
    @DisplayName("Trims whitespace from first and last names upon initialization")
    void createClient_WithWhitespaceInNames_ShouldTrimNames() {
        final String untrimmedFirstName = "   Alexander   ";
        final String untrimmedLastName = "\tShevchenko\n";
        final Client client = new Client(untrimmedFirstName, untrimmedLastName);

        assertEquals(VALID_FIRST_NAME, client.getFirstName(), "First name should be trimmed");
        assertEquals(VALID_LAST_NAME, client.getLastName(), "Last name should be trimmed");
    }

    @ParameterizedTest(name = "Invalid first name: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Throws IllegalArgumentException when first name is null or blank")
    void createClient_WithInvalidFirstName_ShouldThrowException(final String invalidFirstName) {
        final String lastName = VALID_LAST_NAME;

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Client(invalidFirstName, lastName),
                "Should reject blank or null first name"
        );
        assertEquals("Client's first name is required.", exception.getMessage());
    }

    @ParameterizedTest(name = "Invalid last name: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Throws IllegalArgumentException when last name is null or blank")
    void createClient_WithInvalidLastName_ShouldThrowException(final String invalidLastName) {
        final String firstName = VALID_FIRST_NAME;

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Client(firstName, invalidLastName),
                "Should reject blank or null last name"
        );
        assertEquals("Client's last name is required.", exception.getMessage());
    }

    @Test
    @DisplayName("Verify equals and hashCode contract based on client ID")
    void equalsAndHashCode_ShouldBeConsistentWithId() {
        final Client firstClient = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
        final Client secondClient = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);

        assertTrue(firstClient.equals(firstClient), "Client should equal itself");
        assertEquals(firstClient.hashCode(), firstClient.hashCode(), "HashCode should be consistent");

        assertNotEquals(firstClient, secondClient, "Clients with different IDs should not be equal");
        assertFalse(firstClient.equals(null), "Client should not equal null");
        assertFalse(firstClient.equals(new Object()), "Client should not equal an object of different type");
    }

    @Test
    @DisplayName("toString should contain client class name and field values")
    void toString_ShouldContainClientDetails() {
        final Client client = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);

        final String clientString = client.toString();
        assertAll("toString contents",
                () -> assertTrue(clientString.contains("Client"), "Should contain class name"),
                () -> assertTrue(clientString.contains(VALID_FIRST_NAME), "Should contain first name"),
                () -> assertTrue(clientString.contains(VALID_LAST_NAME), "Should contain last name")
        );
    }
}
