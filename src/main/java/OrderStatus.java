public enum OrderStatus {
    CREATED("Created"),
    DIAGNOSED("Diagnosed"),
    APPROVED("Approved"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String description;

    OrderStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
