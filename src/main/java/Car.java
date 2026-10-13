import java.util.Locale;
import java.util.Objects;

/**
 * Client's vehicle.
 * Encapsulates vehicle data and association with the owner (Client).
 * Vehicle identity attributes (vin, make, model, year) are immutable (final),
 * while client ownership remains mutable.
 */
public class Car {

    private final String vin;
    private final String make;
    private final String model;
    private final Integer year;
    private Client client;

    public Car() {
        this.vin = null;
        this.make = null;
        this.model = null;
        this.year = null;
        this.client = null;
    }

    public Car(
            final String vin,
            final String make,
            final String model,
            final Integer year,
            final Client client
    ) {
        if (vin == null || vin.trim().isBlank()) {
            throw new IllegalArgumentException("Car VIN number cannot be empty.");
        }
        if (make == null || make.trim().isBlank()) {
            throw new IllegalArgumentException("Car make cannot be empty.");
        }
        if (model == null || model.trim().isBlank()) {
            throw new IllegalArgumentException("Car model cannot be empty.");
        }
        if (year == null || year < 1886 || year > 2100) {
            throw new IllegalArgumentException("Invalid car manufacture year: " + year);
        }
        this.vin = vin.trim();
        this.make = make.trim();
        this.model = model.trim();
        this.year = year;
        setClient(client);
    }

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public Integer getYear() {
        return year != null ? year : 0;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client (owner) cannot be null.");
        }
        this.client = client;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Car car = (Car) o;
        return vin != null && vin.equalsIgnoreCase(car.vin);
    }

    @Override
    public int hashCode() {
        return vin != null ? vin.toUpperCase(Locale.ROOT).hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Car{" +
                "vin='" + vin + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", client=" + (client != null ? client.getName() : "null") +
                '}';
    }
}
