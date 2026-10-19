package ca.mcgill.ecse428.huddle;

/** The lifecycle status of a game request, matching the feature-file wording. */
public enum GameRequestStatus {
    OPEN("Open"), CONFIRMED("Confirmed"), CANCELLED("Cancelled");

    private final String displayName;
    GameRequestStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static GameRequestStatus fromName(String name) {
        if (name != null) {
            for (GameRequestStatus status : values()) {
                if (status.displayName.equalsIgnoreCase(name.strip())) return status;
            }
        }
        throw new IllegalArgumentException("Unknown game request status: " + name);
    }
}
