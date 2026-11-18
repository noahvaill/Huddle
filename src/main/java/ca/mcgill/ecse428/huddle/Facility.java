package ca.mcgill.ecse428.huddle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A place with courts (e.g. "McGill Sports Complex"), where a facility administrator publishes time slots (ID003).
 * Courts are added through FacilityService, which keeps court names unique across all facilities.
 */
public final class Facility {

    private final String name;
    private final Map<String, Court> courtsByName = new LinkedHashMap<>();

    public Facility(String name) {
        Objects.requireNonNull(name, "Facility name is required");
        this.name = name.strip();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("Facility name must not be blank");
        }
    }

    /** The form a facility or court name is looked up in: stripped and in lower case. */
    static String key(String name) {
        return name == null ? "" : name.strip().toLowerCase(Locale.ROOT);
    }

    /** Adds a court for one sport and returns it. Fails if this facility already has a court with that name. */
    Court addCourt(String courtName, Sport sport) {
        Court court = new Court(courtName, sport);
        if (courtsByName.containsKey(key(court.name()))) {
            throw new IllegalArgumentException(name + " already has a court named " + court.name());
        }
        courtsByName.put(key(court.name()), court);
        return court;
    }

    public String getName() { return name; }

    /** The court with this name, in any case and with surrounding spaces; empty if there is none. */
    public Optional<Court> findCourt(String courtName) {
        return Optional.ofNullable(courtsByName.get(key(courtName)));
    }

    /** The facility's courts, in the order they were added. */
    public List<Court> getCourts() {
        return List.copyOf(courtsByName.values());
    }

    /** The sports that can be played here, each once, in the order their first court was added. */
    public List<Sport> getSports() {
        return courtsByName.values().stream().map(Court::sport).distinct().toList();
    }

    @Override
    public String toString() {
        return name;
    }
}