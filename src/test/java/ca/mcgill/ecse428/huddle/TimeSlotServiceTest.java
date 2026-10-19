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
        return new TimeSlot(court, sport, date, s, s.plusMinutes(45), available);
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
}
