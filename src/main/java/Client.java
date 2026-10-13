import java.util.Objects;

/**
 * Auto service client.
 * Encapsulates client data with validation.
 * Client ID is immutable (final), while name and phone remain mutable.
 */
public class Client {

    private final Long id;
    private String name;
    private String phone;

    public Client() {
        this.id = null;
    }

    public Client(final Long id, final String name, final String phone) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Client ID must be a positive number and cannot be null.");
        }
        this.id = id;
        setName(name);
        setPhone(phone);
    }

    public final Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name cannot be empty.");
        }
        name = name.trim();
        if (!name.matches("^[a-zA-Zа-яА-ЯіІїЇєЄґҐ'\\s\\-]+$")) {
            throw new IllegalArgumentException("Client name contains invalid characters: " + name);
        }
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Client phone number cannot be empty.");
        }
        phone = phone.trim();
        if (!phone.matches("^\\+?[0-9\\s\\-()]{7,20}$")) {
            throw new IllegalArgumentException("Client phone number format is invalid: " + phone);
        }
        this.phone = phone;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
