package carService.domain;

public enum OrderStatus {
    PENDING_DIAGNOSIS,
    DIAGNOSED,
    CLIENT_APPROVED,
    IN_PROGRESS,
    SUCCESSFULLY_COMPLETED,
    CANCELED;

    public boolean isClosed() {
        return this == SUCCESSFULLY_COMPLETED || this == CANCELED;
    }
}