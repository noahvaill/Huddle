package ca.mcgill.ecse428.huddle;

/** What an account is allowed to do in Huddle, matching the feature-file wording (ID001, ID002, ID003). */
public enum Role {
    STUDENT("Student"), FACILITY_ADMINISTRATOR("Facility Administrator");

    private final String displayName;
    Role(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static Role fromName(String name) {
        if (name != null) {
            for (Role role : values()) {
                if (role.displayName.equalsIgnoreCase(name.strip())) return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + name);
    }
}
