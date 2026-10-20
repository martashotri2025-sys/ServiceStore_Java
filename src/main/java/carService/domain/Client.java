package carService.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Client {

    private final UUID id;
    private final String firstName;
    private final String lastName;

    public Client(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Client's first name is required.");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Client's last name is required.");
        }

        this.id = UUID.randomUUID();
        this.firstName = firstName.strip();
        this.lastName = lastName.strip();
    }

}
