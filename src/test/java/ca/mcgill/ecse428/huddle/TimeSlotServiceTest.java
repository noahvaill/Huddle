package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeSlotServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private TimeSlotService service;

    private static TimeSlot slot(String court, Sport sport, LocalDate date, String start, boolean available) {
        LocalTime s = LocalTime.parse(start);
        return new TimeSlot(new Court(court, sport), date, s, s.plusMinutes(45), available);
    }

    @BeforeEach
    void setUp() {
        service = new TimeSlotService();
    }

    @Test
    void listsOnlyAvailableSlotsForSportAndDateSortedByStart() {
        TimeSlot late = slot("Squash Court 2", Sport.SQUASH, DAY, "19:30", true);
        TimeSlot early = slot("Squash Court 1", Sport.SQUASH, DAY, "18:00", true);
        service.addTimeSlot(late);
        service.addTimeSlot(early);
        service.addTimeSlot(slot("Squash Court 1", Sport.SQUASH, DAY, "18:45", false));            // booked
        service.addTimeSlot(slot("Badminton Court 2", Sport.BADMINTON, DAY, "17:00", true));       // other sport
        service.addTimeSlot(slot("Squash Court 1", Sport.SQUASH, DAY.plusDays(1), "18:00", true)); // other day

        assertEquals(List.of(early, late), service.getAvailableTimeSlots("Squash", DAY));
    }

    @Test
    void offeredSportWithNoSlotsReturnsEmptyList() {
        assertTrue(service.getAvailableTimeSlots("Tennis", DAY).isEmpty());
    }

    @Test
    void sportNotOfferedThrows() {
        SportNotOfferedException e = assertThrows(SportNotOfferedException.class,
                () -> service.getAvailableTimeSlots("Curling", DAY));
        assertEquals("Sport not offered", e.getMessage());
    }

    @Test
    void slotsOnTheSameCourtAreListedInStartOrder() {
        TimeSlot second = slot("Squash Court 1", Sport.SQUASH, DAY, "19:30", true);
        TimeSlot first = slot("Squash Court 1", Sport.SQUASH, DAY, "18:00", true);
        service.addTimeSlot(second);
        service.addTimeSlot(first);

        assertEquals(List.of(first, second), service.getAvailableTimeSlots("Squash", DAY));
    }

    @Test
    void simultaneousSlotsAreOrderedByCourtName() {
        TimeSlot court2 = slot("Squash Court 2", Sport.SQUASH, DAY, "18:00", true);
        TimeSlot court1 = slot("Squash Court 1", Sport.SQUASH, DAY, "18:00", true);
        service.addTimeSlot(court2);
        service.addTimeSlot(court1);

        assertEquals(List.of(court1, court2), service.getAvailableTimeSlots("Squash", DAY));
    }

    @Test
    void fullyBookedDayReturnsEmptyList() {
        service.addTimeSlot(slot("Squash Court 1", Sport.SQUASH, DAY, "18:00", false));

        assertTrue(service.getAvailableTimeSlots("Squash", DAY).isEmpty());
    }

    @Test
    void dateWithNoSlotsReturnsEmptyList() {
        service.addTimeSlot(slot("Squash Court 1", Sport.SQUASH, DAY, "18:00", true));

        assertTrue(service.getAvailableTimeSlots("Squash", DAY.plusDays(7)).isEmpty());
    }

    @Test
    void sportNameIsMatchedIgnoringCase() {
        TimeSlot s = slot("Tennis Court 1", Sport.TENNIS, DAY, "10:00", true);
        service.addTimeSlot(s);

        assertEquals(List.of(s), service.getAvailableTimeSlots(" tennis ", DAY));
    }

    @Test
    void nullOrBlankSportThrows() {
        assertThrows(SportNotOfferedException.class, () -> service.getAvailableTimeSlots(null, DAY));
        assertThrows(SportNotOfferedException.class, () -> service.getAvailableTimeSlots("  ", DAY));
    }

    @Test
    void timeSlotTakesItsSportFromTheCourt() {
        assertEquals(Sport.SQUASH, slot("Squash Court 1", Sport.SQUASH, DAY, "18:00", true).sport());
    }

    @Test
    void endMustBeAfterStart() {
        Court c = new Court("Squash Court 1", Sport.SQUASH);
        assertThrows(IllegalArgumentException.class,
                () -> new TimeSlot(c, DAY, LocalTime.of(18, 45), LocalTime.of(18, 0), true));
    }
}
