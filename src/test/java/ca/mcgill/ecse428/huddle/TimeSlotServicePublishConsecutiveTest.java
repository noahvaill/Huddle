package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** ID003, TID017: consecutive publishing must add the whole batch or leave the schedule unchanged. */
class TimeSlotServicePublishConsecutiveTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final Account FRANK = new Account(new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026",
            Role.FACILITY_ADMINISTRATOR);
    private static final Court SQUASH_1 = new Court("Squash Court 1", Sport.SQUASH);
    private static final Court TENNIS_3 = new Court("Tennis Court 3", Sport.TENNIS);

    private TimeSlotService service;
    private TimeSlot existing;

    private static LocalTime at(String time) {
        return LocalTime.parse(time);
    }

    @BeforeEach
    void setUp() {
        service = new TimeSlotService();
        existing = new TimeSlot(SQUASH_1, DAY, at("18:00"), at("18:45"), true);
        service.addTimeSlot(existing);
    }

    @ParameterizedTest
    @CsvSource({"30, 20:00, 4", "45, 21:00, 4", "60, 20:00, 2", "120, 20:00, 1"})
    void publishesEqualAdjacentAvailableSlotsInChronologicalOrder(int interval, String end, int count) {
        List<TimeSlot> published = service.publishConsecutiveTimeSlots(
                FRANK, TENNIS_3, DAY, at("18:00"), at(end), interval);

        assertEquals(count, published.size());
        for (int i = 0; i < count; i++) {
            TimeSlot slot = published.get(i);
            assertEquals(TENNIS_3, slot.court());
            assertEquals(DAY, slot.date());
            assertEquals(at("18:00").plusMinutes((long) i * interval), slot.start());
            assertEquals(at("18:00").plusMinutes((long) (i + 1) * interval), slot.end());
            assertEquals(TimeSlotStatus.AVAILABLE, slot.status());
            assertSame(slot, service.getTimeSlots().get(i + 1));
        }
        assertEquals(at(end), published.getLast().end());
        assertEquals(published, service.getAvailableTimeSlots("Tennis", DAY));
        List<TimeSlot> expected = new ArrayList<>(List.of(existing));
        expected.addAll(published);
        assertEquals(expected, service.getTimeSlots());
        assertThrows(UnsupportedOperationException.class, () -> published.add(existing));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE, 45, 121, Integer.MAX_VALUE})
    void invalidIntervalLeavesTheScheduleUnchanged(int interval) {
        String message = interval <= 0 ? "Interval must be positive"
                : "Time range must contain a whole number of intervals";

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.publishConsecutiveTimeSlots(
                        FRANK, TENNIS_3, DAY, at("18:00"), at("20:00"), interval));

        assertEquals(message, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @ParameterizedTest
    @ValueSource(strings = {"18:00", "17:00", "00:00"})
    void equalReversedOrOvernightRangeIsRejected(String end) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.publishConsecutiveTimeSlots(
                        FRANK, TENNIS_3, DAY, at("18:00"), at(end), 30));

        assertEquals("End time must be after start time", e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @ParameterizedTest
    @ValueSource(strings = {"20:00:01", "20:00:00.000000001"})
    void rangeWithASecondOrNanosecondRemainderIsRejected(String end) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.publishConsecutiveTimeSlots(
                        FRANK, TENNIS_3, DAY, at("18:00"), at(end), 30));

        assertEquals("Time range must contain a whole number of intervals", e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @ParameterizedTest
    @MethodSource("missingInputs")
    void missingInputsUseTheSingleSlotErrorsAndLeaveTheScheduleUnchanged(String message, Account publisher,
            Court court, LocalDate date, LocalTime start, LocalTime end) {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> service.publishConsecutiveTimeSlots(publisher, court, date, start, end, 30));

        assertEquals(message, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    private static Stream<Arguments> missingInputs() {
        return Stream.of(
                Arguments.of("Publisher is required", null, TENNIS_3, DAY, at("18:00"), at("20:00")),
                Arguments.of("Court is required", FRANK, null, DAY, at("18:00"), at("20:00")),
                Arguments.of("Date is required", FRANK, TENNIS_3, null, at("18:00"), at("20:00")),
                Arguments.of("Start time is required", FRANK, TENNIS_3, DAY, null, at("20:00")),
                Arguments.of("End time is required", FRANK, TENNIS_3, DAY, at("18:00"), null));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"STUDENT", "STAFF"})
    void nonAdministratorCannotPublishAnySlots(Role role) {
        Account publisher = new Account(new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", role);

        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishConsecutiveTimeSlots(
                        publisher, TENNIS_3, DAY, at("18:00"), at("20:00"), 30));

        assertEquals(InvalidTimeSlotException.FACILITY_ADMINISTRATOR_ONLY_MESSAGE, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @ParameterizedTest
    @CsvSource({
            "18:10, 18:20, AVAILABLE", "18:40, 18:50, AVAILABLE", "19:10, 19:20, AVAILABLE",
            "18:10, 18:20, BOOKED", "18:40, 18:50, BOOKED", "19:10, 19:20, BOOKED"
    })
    void conflictInFirstMiddleOrLastSlotRejectsTheEntireBatch(String start, String end, TimeSlotStatus status) {
        TimeSlot conflict = new TimeSlot(TENNIS_3, DAY, at(start), at(end), status);
        service.addTimeSlot(conflict);
        List<TimeSlot> before = service.getTimeSlots();
        List<TimeSlot> availableBefore = service.getAvailableTimeSlots("Tennis", DAY);

        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishConsecutiveTimeSlots(
                        FRANK, TENNIS_3, DAY, at("18:00"), at("19:30"), 30));

        assertEquals(InvalidTimeSlotException.OVERLAPPING_SLOT_MESSAGE, e.getMessage());
        assertEquals(before, service.getTimeSlots());
        assertEquals(availableBefore, service.getAvailableTimeSlots("Tennis", DAY));
        assertEquals(status, conflict.status());
        assertSame(conflict, service.getTimeSlots().getLast());
    }

    @Test
    void existingSlotsTouchingEitherBoundaryDoNotBlockTheBatch() {
        TimeSlot before = new TimeSlot(TENNIS_3, DAY, at("17:30"), at("18:00"), TimeSlotStatus.BOOKED);
        TimeSlot after = new TimeSlot(TENNIS_3, DAY, at("20:00"), at("20:30"), TimeSlotStatus.BOOKED);
        service.addTimeSlot(before);
        service.addTimeSlot(after);

        List<TimeSlot> published = service.publishConsecutiveTimeSlots(
                FRANK, TENNIS_3, DAY, at("18:00"), at("20:00"), 30);

        assertEquals(4, published.size());
        assertEquals(published, service.getAvailableTimeSlots("Tennis", DAY));
        assertEquals(TimeSlotStatus.BOOKED, before.status());
        assertEquals(TimeSlotStatus.BOOKED, after.status());
        assertEquals(7, service.getTimeSlots().size());
    }

    @Test
    void identicalTimesOnAnotherCourtOrDateDoNotBlockTheBatch() {
        service.addTimeSlot(new TimeSlot(TENNIS_3, DAY.plusDays(1), at("18:00"), at("20:00"), false));

        List<TimeSlot> published = service.publishConsecutiveTimeSlots(
                FRANK, TENNIS_3, DAY, at("18:00"), at("20:00"), 30);

        assertEquals(4, published.size());
        assertEquals(published, service.getAvailableTimeSlots("Tennis", DAY));
        assertEquals(List.of(existing), service.getAvailableTimeSlots("Squash", DAY));
        assertEquals(6, service.getTimeSlots().size());
    }

    @Test
    void consecutiveAndSingleSlotPublishingSeeEachOthersSlots() {
        List<TimeSlot> published = service.publishConsecutiveTimeSlots(
                FRANK, TENNIS_3, DAY, at("18:00"), at("20:00"), 30);
        List<TimeSlot> before = service.getTimeSlots();

        assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("19:45"), at("20:15")));
        assertThrows(InvalidTimeSlotException.class,
                () -> service.publishConsecutiveTimeSlots(
                        FRANK, TENNIS_3, DAY, at("18:00"), at("20:00"), 30));
        assertEquals(before, service.getTimeSlots());

        service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("20:00"), at("20:30"));
        assertEquals(4, published.size());
        assertEquals(6, service.getTimeSlots().size());
    }
}
