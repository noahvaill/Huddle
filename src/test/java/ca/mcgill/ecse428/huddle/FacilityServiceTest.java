package ca.mcgill.ecse428.huddle;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for facilities with their courts and sports (ID003, TID015). */
class FacilityServiceTest {

    private static final String COMPLEX = "McGill Sports Complex";

    private FacilityService service;

    @BeforeEach
    void setUp() {
        // The courts of the ID003 feature's Background
        service = new FacilityService();
        service.addCourt(COMPLEX, "Squash Court 1", Sport.SQUASH);
        service.addCourt(COMPLEX, "Badminton Court 2", Sport.BADMINTON);
        service.addCourt(COMPLEX, "Tennis Court 3", Sport.TENNIS);
    }

    @Test
    void facilityIsCreatedWithItsFirstCourtAndReusedAfter() {
        assertEquals(1, service.getFacilities().size());
        assertEquals(COMPLEX, service.getFacilities().get(0).getName());
    }

    @Test
    void facilityListsItsCourtsInTheOrderTheyWereAdded() {
        Facility complex = service.findFacility(COMPLEX).orElseThrow();
        assertEquals(List.of(
                new Court("Squash Court 1", Sport.SQUASH),
                new Court("Badminton Court 2", Sport.BADMINTON),
                new Court("Tennis Court 3", Sport.TENNIS)), complex.getCourts());
    }

    @Test
    void facilityListsEachSportOnce() {
        service.addCourt(COMPLEX, "Squash Court 2", Sport.SQUASH);
        Facility complex = service.findFacility(COMPLEX).orElseThrow();
        assertEquals(List.of(Sport.SQUASH, Sport.BADMINTON, Sport.TENNIS), complex.getSports());
    }

    @Test
    void courtIsFoundByNameWithItsSport() {
        Court court = service.findCourt("Tennis Court 3").orElseThrow();
        assertEquals("Tennis Court 3", court.name());
        assertEquals(Sport.TENNIS, court.sport());
    }

    @Test
    void courtAndFacilityAreFoundInAnyCaseAndWithSurroundingSpaces() {
        assertTrue(service.findCourt("  tennis court 3 ").isPresent());
        assertTrue(service.findFacility(" mcgill sports complex ").isPresent());
    }

    @Test
    void unknownOrMissingNamesAreNotFound() {
        assertTrue(service.findCourt("Tennis Court 9").isEmpty());
        assertTrue(service.findCourt(null).isEmpty());
        assertTrue(service.findFacility("Currie Gym").isEmpty());
        assertTrue(service.findFacility(null).isEmpty());
    }

    @Test
    void facilityOfACourtIsFound() {
        service.addCourt("Currie Gym", "Basketball Court 1", Sport.BASKETBALL);
        assertEquals("Currie Gym", service.findFacilityOfCourt("Basketball Court 1").orElseThrow().getName());
        assertEquals(COMPLEX, service.findFacilityOfCourt("Squash Court 1").orElseThrow().getName());
    }

    @Test
    void courtNameCanBeUsedOnlyOnceInAFacility() {
        assertThrows(IllegalArgumentException.class, () -> service.addCourt(COMPLEX, "squash court 1", Sport.SQUASH));
        assertEquals(3, service.findFacility(COMPLEX).orElseThrow().getCourts().size());
    }

    @Test
    void courtNameCanBeUsedOnlyOnceAcrossFacilities() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addCourt("Currie Gym", "Squash Court 1", Sport.SQUASH));
        // Nothing is added, not even the new facility
        assertTrue(service.findFacility("Currie Gym").isEmpty());
    }

    @Test
    void invalidCourtAddsNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.addCourt("Currie Gym", " ", Sport.SQUASH));
        assertThrows(NullPointerException.class, () -> service.addCourt("Currie Gym", "Court 1", null));
        assertTrue(service.findFacility("Currie Gym").isEmpty());
    }

    @Test
    void emptyFacilityCanBeAddedAndFilledLater() {
        Facility gym = service.addFacility("Currie Gym");
        assertTrue(gym.getCourts().isEmpty());
        service.addCourt("Currie Gym", "Basketball Court 1", Sport.BASKETBALL);
        assertSame(gym, service.findFacility("Currie Gym").orElseThrow());
        assertEquals(1, gym.getCourts().size());
    }

    @Test
    void duplicateOrBlankFacilityNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.addFacility("mcgill sports complex"));
        assertThrows(IllegalArgumentException.class, () -> service.addFacility("  "));
        assertThrows(NullPointerException.class, () -> service.addFacility(null));
    }
}