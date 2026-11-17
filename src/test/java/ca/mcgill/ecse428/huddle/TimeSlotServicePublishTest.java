package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID003: a facility administrator publishes one time slot for a court, TimeSlotService.publishTimeSlot (TID016). */
class TimeSlotServicePublishTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final Account FRANK = new Account(new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026",
            Role.FACILITY_ADMINISTRATOR);
    private static final Account ALICE = new Account(new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026",
            Role.STUDENT);
    private static final Court SQUASH_1 = new Court("Squash Court 1", Sport.SQUASH);
    private static final Court SQUASH_2 = new Court("Squash Court 2", Sport.SQUASH);
    private static final Court TENNIS_3 = new Court("Tennis Court 3", Sport.TENNIS);

    private TimeSlotService service;
    private TimeSlot existing;

    private static LocalTime at(String time) {
        return LocalTime.parse(time);
    }

    @BeforeEach
    void setUp() {
        service = new TimeSlotService();
        // The time slot from the ID003 feature background
        existing = new TimeSlot(SQUASH_1, DAY, at("18:00"), at("18:45"), true);
        service.addTimeSlot(existing);
    }

    @Test
    void facilityAdministratorPublishesAnAvailableSlot() {
        TimeSlot slot = service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:00"), at("19:00"));

        assertEquals(new TimeSlot(TENNIS_3, DAY, at("18:00"), at("19:00"), true), slot);
        assertEquals(TimeSlotStatus.AVAILABLE, slot.status());
        assertEquals(List.of(existing, slot), service.getTimeSlots());
        assertEquals(List.of(slot), service.getAvailableTimeSlots("Tennis", DAY));
        assertEquals("1 time slot published", TimeSlotService.ONE_SLOT_PUBLISHED_MESSAGE);
    }

    @Test
    void studentCannotPublish() {
        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(ALICE, TENNIS_3, DAY, at("18:00"), at("19:00")));

        assertEquals(InvalidTimeSlotException.FACILITY_ADMINISTRATOR_ONLY_MESSAGE, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @Test
    void staffMemberCannotPublish() {
        Account staff = new Account(new User("Gina Park"), "gina.park@mcgill.ca", "Staff!2026", Role.STAFF);

        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(staff, TENNIS_3, DAY, at("18:00"), at("19:00")));

        assertEquals(InvalidTimeSlotException.FACILITY_ADMINISTRATOR_ONLY_MESSAGE, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @Test
    void slotOverlappingAnExistingSlotOnTheSameCourtIsRejected() {
        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:30"), at("19:15")));

        assertEquals(InvalidTimeSlotException.OVERLAPPING_SLOT_MESSAGE, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @Test
    void slotInsideOrAroundAnExistingSlotIsRejected() {
        assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:10"), at("18:30")));
        assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("17:00"), at("19:00")));
        assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:00"), at("18:45")));

        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @Test
    void bookedSlotAlsoBlocksAnOverlappingSlot() {
        existing.book();

        assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:30"), at("19:15")));
    }

    @Test
    void slotThatOnlyTouchesAnExistingSlotIsPublished() {
        TimeSlot before = service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("17:15"), at("18:00"));
        TimeSlot after = service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:45"), at("19:30"));

        assertEquals(List.of(existing, before, after), service.getTimeSlots());
    }

    @Test
    void sameTimesOnAnotherCourtOrAnotherDayArePublished() {
        TimeSlot otherCourt = service.publishTimeSlot(FRANK, SQUASH_2, DAY, at("18:00"), at("18:45"));
        TimeSlot otherDay = service.publishTimeSlot(FRANK, SQUASH_1, DAY.plusDays(1), at("18:00"), at("18:45"));

        assertTrue(service.getTimeSlots().containsAll(List.of(otherCourt, otherDay)));
    }

    @Test
    void endTimeNotAfterStartTimeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("19:00"), at("18:00")));
        assertThrows(IllegalArgumentException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:00"), at("18:00")));

        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @Test
    void missingPublisherOrSlotDetailsAreRejected() {
        assertThrows(NullPointerException.class,
                () -> service.publishTimeSlot(null, TENNIS_3, DAY, at("18:00"), at("19:00")));
        assertThrows(NullPointerException.class,
                () -> service.publishTimeSlot(FRANK, null, DAY, at("18:00"), at("19:00")));
        assertThrows(NullPointerException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, null, at("18:00"), at("19:00")));

        assertEquals(List.of(existing), service.getTimeSlots());
    }
}
