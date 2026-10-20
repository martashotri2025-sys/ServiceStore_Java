package carService.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Year;

@Getter
@EqualsAndHashCode(of = "vin")
@ToString
public class Car {

    private static final int MIN_PRODUCTION_YEAR = 1886;

    private final String vin;
    private final String make;
    private final String model;
    private final int productionYear;
    private Client owner;

    public Car(String vin, String make, String model, int productionYear, Client owner) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("Vehicle Identification Number cannot be blank.");
        }

        int maxYear = Year.now().getValue() + 1;
        if (productionYear < MIN_PRODUCTION_YEAR || productionYear > maxYear) {
            throw new IllegalArgumentException("Production year " + productionYear + " is out of valid bounds.");
        }

        if (make == null || make.isBlank()) {
            throw new IllegalArgumentException("Car make is mandatory.");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Car model is mandatory.");
        }

        this.vin = vin.trim();
        this.make = make.trim();
        this.model = model.trim();
        this.productionYear = productionYear;
        changeOwner(owner);
    }

    public void changeOwner(Client newOwner) {
        if (newOwner == null) {
            throw new IllegalArgumentException("Car must have a valid owner.");
        }
        this.owner = newOwner;
    }

    public enum MechanicSpecialization {
        ENGINE_REPAIR,
        ELECTRICAL,
        SUSPENSION,
        BRAKE_SYSTEM,
        TRANSMISSION,
        DIAGNOSTICS,
        BODY_REPAIR,
        GENERAL_MAINTENANCE;

        public static MechanicSpecialization fromValue(final String value) {
            if (value == null || value.trim().isBlank()) {
                throw new IllegalArgumentException("Specialization value cannot be null or blank.");
            }
            final String normalized = value.trim();
            for (final MechanicSpecialization spec : values()) {
                if (spec.name().equalsIgnoreCase(normalized)) {
                    return spec;
                }
            }
            throw new IllegalArgumentException("Unknown mechanic specialization: " + value);
        }
    }
}