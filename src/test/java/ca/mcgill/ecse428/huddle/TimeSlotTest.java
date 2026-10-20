package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class TimeSlotTest {

    private static final Court COURT = new Court("Squash Court 1", Sport.SQUASH);
    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    private static TimeSlot slot(boolean available) {
        return new TimeSlot(COURT, DAY, LocalTime.of(18, 0), LocalTime.of(18, 45), available);
    }

    @Test
    void booleanConstructorSetsTheStatus() {
        assertEquals(TimeSlotStatus.AVAILABLE, slot(true).status());
        assertEquals(TimeSlotStatus.BOOKED, slot(false).status());
    }

    @Test
    void bookingAnAvailableSlotMakesItBooked() {
        TimeSlot s = slot(true);
        s.book();
        assertEquals(TimeSlotStatus.BOOKED, s.status());
        assertFalse(s.available());
    }

    @Test
    void bookingABookedSlotFails() {
        TimeSlot s = slot(false);
        IllegalStateException e = assertThrows(IllegalStateException.class, s::book);
        assertEquals("Time slot is already booked", e.getMessage());
    }

    @Test
    void equalityIgnoresStatus() {
        TimeSlot a = slot(true);
        TimeSlot b = slot(false);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void nullStatusIsRejected() {
        assertThrows(NullPointerException.class,
                () -> new TimeSlot(COURT, DAY, LocalTime.of(18, 0), LocalTime.of(18, 45), (TimeSlotStatus) null));
    }

    @Test
    void statusFromName() {
        assertEquals(TimeSlotStatus.BOOKED, TimeSlotStatus.fromName(" booked "));
        assertEquals(TimeSlotStatus.AVAILABLE, TimeSlotStatus.fromName("Available"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> TimeSlotStatus.fromName("Taken"))
                .getMessage().contains("Taken"));
    }
}