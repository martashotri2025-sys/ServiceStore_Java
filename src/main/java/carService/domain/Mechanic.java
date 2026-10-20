package carService.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Mechanic {

    private final UUID id;
    private final String fullName;
    private boolean available;

    public Mechanic(String fullName) {
        this.id = UUID.randomUUID();

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Mechanic full name cannot be empty.");
        }

        this.fullName = fullName.trim();
        this.available = true;
    }

    void assignToOrder() {
        if (!this.available) {
            throw new IllegalStateException("Mechanic " + fullName + " is currently busy.");
        }
        this.available = false;
    }

    void releaseFromOrder() {
        if (this.available) {
            throw new IllegalStateException("Mechanic " + fullName + " is already available.");
        }
        this.available = true;
    }
}