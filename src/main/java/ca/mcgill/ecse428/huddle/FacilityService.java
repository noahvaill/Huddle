package ca.mcgill.ecse428.huddle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The registry of facilities with their courts and sports (ID003, TID015).
 * Publishing time slots looks a court up here by its name (e.g. "Tennis Court 3"), so a court name can be used
 * only once across all facilities.
 */
public class FacilityService {

    private final Map<String, Facility> facilitiesByName = new LinkedHashMap<>();

    /** Adds an empty facility and returns it. Fails if a facility with that name already exists, in any case. */
    public Facility addFacility(String facilityName) {
        Facility facility = new Facility(facilityName);
        if (facilitiesByName.containsKey(Facility.key(facility.getName()))) {
            throw new IllegalArgumentException("A facility named " + facility.getName() + " already exists");
        }
        facilitiesByName.put(Facility.key(facility.getName()), facility);
        return facility;
    }

    /**
     * Adds a court for one sport to a facility and returns it, e.g. a row of "the following courts exist at
     * "McGill Sports Complex":". The facility is created if it does not exist yet.
     * Fails, and adds nothing, if any facility already has a court with that name.
     */
    public Court addCourt(String facilityName, String courtName, Sport sport) {
        Court court = new Court(courtName, sport); // checks the name and sport before a facility is created
        Optional<Facility> owner = findFacilityOfCourt(court.name());
        if (owner.isPresent()) {
            throw new IllegalArgumentException(owner.get().getName() + " already has a court named " + court.name());
        }
        Facility facility = findFacility(facilityName).orElseGet(() -> addFacility(facilityName));
        return facility.addCourt(court.name(), sport);
    }

    /** The facility with this name, in any case and with surrounding spaces; empty if there is none. */
    public Optional<Facility> findFacility(String facilityName) {
        return Optional.ofNullable(facilitiesByName.get(Facility.key(facilityName)));
    }

    /** The court with this name in any facility, in any case and with surrounding spaces; empty if there is none. */
    public Optional<Court> findCourt(String courtName) {
        return findFacilityOfCourt(courtName).flatMap(f -> f.findCourt(courtName));
    }

    /** The facility that has the court with this name; empty if no facility has it. */
    public Optional<Facility> findFacilityOfCourt(String courtName) {
        return facilitiesByName.values().stream().filter(f -> f.findCourt(courtName).isPresent()).findFirst();
    }

    /** All facilities, in the order they were added. */
    public List<Facility> getFacilities() {
        return List.copyOf(facilitiesByName.values());
    }
}