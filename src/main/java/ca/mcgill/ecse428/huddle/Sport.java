package ca.mcgill.ecse428.huddle;

import java.util.Arrays;
import java.util.List;


public enum Sport {
    SQUASH("Squash"), TENNIS("Tennis"), PICKLEBALL("Pickleball"), BADMINTON("Badminton"), BASKETBALL("Basketball");

    private final String displayName;
    Sport(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static Sport fromName(String name) {
        if (name != null) {
            for (Sport s : values()) {
                if (s.displayName.equalsIgnoreCase(name.trim())) return s;
            }
        }
        throw new SportNotOfferedException(name);
    }

    public static List<String> offeredSportNames() {
        return Arrays.stream(values()).map(Sport::getDisplayName).toList();
    }
}
