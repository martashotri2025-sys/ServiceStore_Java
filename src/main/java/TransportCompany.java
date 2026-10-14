import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransportCompany {

    private String name;
    private Client client;
    private List<Car> transport;

    public TransportCompany() {
        this(null, null, new ArrayList<>());
    }

    public TransportCompany(final String name) {
        this(name, null, new ArrayList<>());
    }

    public TransportCompany(final Client client) {
        this(client != null ? client.getName() : null, client, new ArrayList<>());
    }

    public TransportCompany(final String name, final Client client) {
        this(name, client, new ArrayList<>());
    }

    public TransportCompany(final String name, final List<Car> transport) {
        this(name, null, transport);
    }

    public TransportCompany(final Client client, final List<Car> transport) {
        this(client != null ? client.getName() : null, client, transport);
    }

    public TransportCompany(final Long clientId, final String name, final String phone) {
        this(name, new Client(clientId, name, phone), new ArrayList<>());
    }


    public TransportCompany(final String name, final Client client, final List<Car> transport) {
        this.name = name;
        this.client = client;
        this.transport = transport != null ? transport : new ArrayList<>();
    }

    public void addTransport(final Car car) {
        if (car != null) {
            this.transport.add(car);
        }
    }

    public void addCar(final Car car) {
        addTransport(car);
    }

    public void addBus(final Car bus) {
        addTransport(bus);
    }

    public void addTaxi(final Car taxi) {
        addTransport(taxi);
    }

    public Car createBus(final String vin, final String make, final String model, final int year) {
        final Car bus = new Car(vin, make, model, year, this.client);
        addTransport(bus);
        return bus;
    }

    public Car createTaxi(final String vin, final String make, final String model, final int year) {
        final Car taxi = new Car(vin, make, model, year, this.client);
        addTransport(taxi);
        return taxi;
    }

    public boolean removeTransport(final Car car) {
        return this.transport.remove(car);
    }

    public List<Car> getTransport() {
        return transport;
    }

    public void setTransport(final List<Car> transport) {
        this.transport = transport != null ? transport : new ArrayList<>();
    }

    public List<Car> getAllTransport() {
        return transport;
    }

    public List<Car> getBuses() {
        return transport;
    }

    public List<Car> getTaxis() {
        return transport;
    }

    public List<Car> sendToRepair(final int amount) {
        if (amount <= 0 || transport.isEmpty()) {
            return new ArrayList<>();
        }
        final int count = Math.min(amount, transport.size());
        return new ArrayList<>(transport.subList(0, count));
    }

    public List<Car> sendToServiceStore(final int amount) {
        return sendToRepair(amount);
    }g

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
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
        final TransportCompany that = (TransportCompany) o;
        return Objects.equals(name, that.name) && Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, client);
    }

    @Override
    public String toString() {
        return "TransportCompany{" +
                "name='" + name + '\'' +
                ", client=" + (client != null ? client.getName() : "null") +
                ", transportCount=" + transport.size() +
                '}';
    }
}