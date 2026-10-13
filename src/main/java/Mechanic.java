import java.util.Objects;

/**
 * Auto service mechanic.
 * Encapsulates mechanic information and busy status.
 * Mechanic ID is immutable (final).
 */
public class Mechanic {

    private final Long id;
    private String name;
    private String specialization;
    private Boolean busy;

    public Mechanic() {
        this.id = null;
        this.busy = false;
    }

    public Mechanic(
            final Long id,
            final String name,
            final String specialization
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Mechanic ID must be a positive number and cannot be null.");
        }
        this.id = id;
        setName(name);
        setSpecialization(specialization);
        this.busy = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Mechanic name cannot be empty.");
        }
        name = name.trim();
        if (!name.matches("^[a-zA-Zа-яА-ЯіІїЇєЄґҐ'\\s\\-]+$")) {
            throw new IllegalArgumentException("Mechanic name contains invalid characters: " + name);
        }
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(final String specialization) {
        if (specialization == null || specialization.trim().isBlank()) {
            this.specialization = "General";
        } else {
            this.specialization = specialization.trim();
        }
    }

    public void setBusy(final Boolean busy) {
        this.busy = busy;
    }

    /**
     * Assign mechanic to execute an order.
     * Throws an exception if the mechanic is already busy.
     */
    public void assign() {
        if (Boolean.TRUE.equals(this.busy)) {
            throw new CarServiceException("Mechanic " + name + " is already busy with another active order.");
        }
        this.busy = Boolean.TRUE;
    }

    /**
     * Release mechanic after order completion or cancellation.
     */
    public void release() {
        this.busy = Boolean.FALSE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Mechanic mechanic = (Mechanic) o;
        return Objects.equals(id, mechanic.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Mechanic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", specialization='" + specialization + '\'' +
                ", busy=" + busy +
                '}';
    }
}
