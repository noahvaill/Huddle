package ca.mcgill.ecse428.huddle;

import java.util.Objects;

/** A bookable court (e.g. "Squash Court 1") on which one sport is played. */
public record Court(String name, Sport sport) {

    public Court {
        Objects.requireNonNull(name, "Court name is required");
        Objects.requireNonNull(sport, "Court sport is required");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Court name must not be blank");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
