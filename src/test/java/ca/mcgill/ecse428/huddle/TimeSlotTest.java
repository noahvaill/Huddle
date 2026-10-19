package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class TimeSlotTest {

    private final Court court1 = new Court("Squash Court 1", new Sport("Squash"));
    private final Court court2 = new Court("Squash Court 2", new Sport("Squash"));
    private final LocalDate day = LocalDate.of(2026, 10, 15);

    private TimeSlot slot(Court court, LocalDate date, String start, String end) {
        return new TimeSlot(court, date, LocalTime.parse(start), LocalTime.parse(end));
    }

    @Test
    void newSlotIsAvailableByDefault() {
        TimeSlot slot = slot(court1, day, "18:00", "18:45");
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertTrue(slot.isAvailable());
    }

    @Test
    void sportComesFromCourt() {
        assertEquals(new Sport("Squash"), slot(court1, day, "18:00", "18:45").getSport());
    }

    @Test
    void slotCanBeCreatedAsBooked() {
        TimeSlot slot = new TimeSlot(court1, day, LocalTime.of(18, 45), LocalTime.of(19, 30), SlotStatus.BOOKED);
        assertFalse(slot.isAvailable());
    }

    @Test
    void bookingMarksSlotAsBooked() {
        TimeSlot slot = slot(court1, day, "18:00", "18:45");
        slot.book();
        assertEquals(SlotStatus.BOOKED, slot.getStatus());
    }

    @Test
    void bookingABookedSlotFails() {
        TimeSlot slot = slot(court1, day, "18:00", "18:45");
        slot.book();
        assertThrows(IllegalStateException.class, slot::book);
    }

    @Test
    void endMustBeAfterStart() {
        assertThrows(IllegalArgumentException.class, () -> slot(court1, day, "18:45", "18:00"));
        assertThrows(IllegalArgumentException.class, () -> slot(court1, day, "18:00", "18:00"));
    }

    @Test
    void overlappingSlotsOnSameCourtAndDateOverlap() {
        assertTrue(slot(court1, day, "18:00", "18:45").overlaps(slot(court1, day, "18:30", "19:15")));
    }

    @Test
    void backToBackSlotsDoNotOverlap() {
        assertFalse(slot(court1, day, "18:00", "18:45").overlaps(slot(court1, day, "18:45", "19:30")));
    }

    @Test
    void slotsOnDifferentCourtsOrDatesDoNotOverlap() {
        TimeSlot base = slot(court1, day, "18:00", "18:45");
        assertFalse(base.overlaps(slot(court2, day, "18:00", "18:45")));
        assertFalse(base.overlaps(slot(court1, day.plusDays(1), "18:00", "18:45")));
    }
}
