/**
 * Professional specializations for auto service mechanics.
 */
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