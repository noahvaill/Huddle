package ca.mcgill.ecse428.huddle;

import java.util.Objects;

/** A Huddle user, identified by their full name (e.g. "Alice Chen"). */
public record User(String name) {

    public User {
        Objects.requireNonNull(name, "User name is required");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("User name must not be blank");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
