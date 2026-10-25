package ca.mcgill.ecse428.huddle;

/** Whether a time slot can still be booked, matching the feature-file wording. */
public enum TimeSlotStatus {
    AVAILABLE("Available"), BOOKED("Booked");

    private final String displayName;
    TimeSlotStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static TimeSlotStatus fromName(String name) {
        if (name != null) {
            for (TimeSlotStatus status : values()) {
                if (status.displayName.equalsIgnoreCase(name.strip())) return status;
            }
        }
        throw new IllegalArgumentException("Unknown time slot status: " + name);
    }
}