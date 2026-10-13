/**
 * Professional specializations for auto service mechanics.
 */
public enum MechanicSpecialization {
    ENGINE_REPAIR("Engine Repair"),
    ELECTRICAL("Electrical Systems"),
    SUSPENSION("Suspension & Steering"),
    BRAKE_SYSTEM("Brake Systems"),
    TRANSMISSION("Transmission"),
    DIAGNOSTICS("Computer Diagnostics"),
    BODY_REPAIR("Body Repair"),
    GENERAL_MAINTENANCE("General Maintenance");

    private final String displayName;

    MechanicSpecialization(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Safely looks up a specialization constant by string value case-insensitively.
     *
     * @param value string name or display name of the specialization
     * @return matching MechanicSpecialization constant
     * @throws IllegalArgumentException if the input is null, blank, or not found
     */
    public static MechanicSpecialization fromValue(final String value) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("Specialization value cannot be null or blank.");
        }
        final String normalized = value.trim();
        for (final MechanicSpecialization spec : values()) {
            if (spec.name().equalsIgnoreCase(normalized) || spec.displayName.equalsIgnoreCase(normalized)) {
                return spec;
            }
        }
        throw new IllegalArgumentException("Unknown mechanic specialization: " + value);
    }
}
