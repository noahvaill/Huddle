package ca.mcgill.ecse428.huddle;

import java.util.Objects;

/** A sport that can be played at a Huddle facility, identified by its name (e.g. "Squash"). */
public record Sport(String name) {

    public Sport {
        Objects.requireNonNull(name, "Sport name is required");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Sport name must not be blank");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
