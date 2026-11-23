package ca.mcgill.ecse428.huddle;

import java.util.Arrays;
import java.util.List;

/** A user's level in one sport (ID014), matching the feature-file wording. */
public enum SkillLevel {
    BEGINNER("Beginner"), INTERMEDIATE("Intermediate"), ADVANCED("Advanced");

    private final String displayName;
    SkillLevel(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static SkillLevel fromName(String name) {
        if (name != null) {
            for (SkillLevel level : values()) {
                if (level.displayName.equalsIgnoreCase(name.strip())) return level;
            }
        }
        throw new IllegalArgumentException("Unknown skill level: " + name);
    }

    public static List<String> offeredLevelNames() {
        return Arrays.stream(values()).map(SkillLevel::getDisplayName).toList();
    }
}
