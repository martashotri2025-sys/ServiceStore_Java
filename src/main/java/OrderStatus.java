public enum OrderStatus {
    CREATED,
    DIAGNOSED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static OrderStatus fromValue(final String value) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("Order status value cannot be null or blank.");
        }
        final String normalized = value.trim();
        for (final OrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }
}