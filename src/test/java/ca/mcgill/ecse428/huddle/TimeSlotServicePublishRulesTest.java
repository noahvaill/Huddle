package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ID003, TID020: the rules for publishing one time slot, TimeSlotService.publishTimeSlot. Covers the order in which
 * the rules are checked, slots published earlier, and how a published slot is then viewed (ID004) and booked (ID010).
 */
class TimeSlotServicePublishRulesTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final Account FRANK = new Account(new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026",
            Role.FACILITY_ADMINISTRATOR);
    private static final Account ALICE = new Account(new User("Alice Chen"), "alice.chen@mail.mcgill.ca",
            "Racket!2026", Role.STUDENT);
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

    // Order of the rules: the administrator check comes first, so a student learns nothing else about the request

    @Test
    void nonAdministratorPublishingAnOverlappingSlotGetsTheAdministratorError() {
        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(ALICE, SQUASH_1, DAY, at("18:30"), at("19:15")));

        assertEquals(InvalidTimeSlotException.FACILITY_ADMINISTRATOR_ONLY_MESSAGE, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @Test
    void nonAdministratorWithInvalidTimesGetsTheAdministratorError() {
        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(ALICE, TENNIS_3, DAY, at("19:00"), at("18:00")));

        assertEquals(InvalidTimeSlotException.FACILITY_ADMINISTRATOR_ONLY_MESSAGE, e.getMessage());
    }

    @Test
    void nonAdministratorWithMissingDetailsGetsTheAdministratorError() {
        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(ALICE, null, null, null, null));

        assertEquals(InvalidTimeSlotException.FACILITY_ADMINISTRATOR_ONLY_MESSAGE, e.getMessage());
    }

    @Test
    void invalidTimesAreReportedBeforeAnOverlap() {
        // 18:30-18:00 would sit inside the existing 18:00-18:45 slot if the times were the right way round
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:30"), at("18:00")));

        assertEquals("End time must be after start time", e.getMessage());
    }

    // Slot details

    @ParameterizedTest
    @ValueSource(strings = {"18:00", "17:00", "00:00"})
    void endTimeNotAfterStartTimeIsRejectedWithItsMessage(String end) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:00"), at(end)));

        assertEquals("End time must be after start time", e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    @ParameterizedTest
    @MethodSource("missingInputs")
    void missingInputsAreRejectedWithTheirMessage(String message, Account publisher, Court court, LocalDate date,
            LocalTime start, LocalTime end) {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> service.publishTimeSlot(publisher, court, date, start, end));

        assertEquals(message, e.getMessage());
        assertEquals(List.of(existing), service.getTimeSlots());
    }

    private static Stream<Arguments> missingInputs() {
        return Stream.of(
                Arguments.of("Publisher is required", null, TENNIS_3, DAY, at("18:00"), at("19:00")),
                Arguments.of("Court is required", FRANK, null, DAY, at("18:00"), at("19:00")),
                Arguments.of("Date is required", FRANK, TENNIS_3, null, at("18:00"), at("19:00")),
                Arguments.of("Start time is required", FRANK, TENNIS_3, DAY, null, at("19:00")),
                Arguments.of("End time is required", FRANK, TENNIS_3, DAY, at("18:00"), null));
    }

    @ParameterizedTest
    @CsvSource({"18:00, 18:01", "23:00, 23:59", "00:00, 00:30"})
    void shortSlotsAndSlotsAtEitherEndOfTheDayArePublished(String start, String end) {
        TimeSlot slot = service.publishTimeSlot(FRANK, TENNIS_3, DAY, at(start), at(end));

        assertEquals(at(start), slot.start());
        assertEquals(at(end), slot.end());
        assertEquals(List.of(existing, slot), service.getTimeSlots());
    }

    // Overlap with slots published earlier

    @Test
    void slotOverlappingAnEarlierPublishedSlotIsRejected() {
        TimeSlot first = service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:00"), at("19:00"));

        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:30"), at("19:30")));

        assertEquals(InvalidTimeSlotException.OVERLAPPING_SLOT_MESSAGE, e.getMessage());
        assertEquals(List.of(existing, first), service.getTimeSlots());
    }

    @Test
    void publishingTheSameSlotTwiceIsRejected() {
        service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:00"), at("19:00"));

        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:00"), at("19:00")));

        assertEquals(InvalidTimeSlotException.OVERLAPPING_SLOT_MESSAGE, e.getMessage());
        assertEquals(2, service.getTimeSlots().size());
    }

    @Test
    void rejectedPublishLeavesNothingBehindSoACorrectedOneSucceeds() {
        assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:30"), at("19:15")));

        TimeSlot corrected = service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:45"), at("19:30"));

        assertEquals(List.of(existing, corrected), service.getTimeSlots());
    }

    @Test
    void slotsFillingADayBackToBackArePublished() {
        TimeSlot morning = service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("08:00"), at("12:00"));
        TimeSlot afternoon = service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("12:00"), at("18:00"));
        TimeSlot evening = service.publishTimeSlot(FRANK, SQUASH_1, DAY, at("18:45"), at("22:00"));

        assertEquals(List.of(existing, morning, afternoon, evening), service.getTimeSlots());
    }

    // What happens to a published slot afterwards

    @Test
    void publishedSlotsAreListedWithTheOtherAvailableSlotsByStartThenCourt() {
        TimeSlot late = service.publishTimeSlot(FRANK, SQUASH_2, DAY, at("18:00"), at("18:45"));
        TimeSlot early = service.publishTimeSlot(FRANK, SQUASH_2, DAY, at("17:15"), at("18:00"));

        assertEquals(List.of(early, existing, late), service.getAvailableTimeSlots("Squash", DAY));
    }

    @Test
    void publishedSlotIsBookedWhenAGameOnItIsConfirmedAndStillBlocksOverlaps() {
        TimeSlot slot = service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:00"), at("19:00"));
        GameRequestService games = new GameRequestService();
        GameRequest request = games.postRequest(new User("Alice Chen"), slot, 1, 1);

        games.joinRequest(new User("Bob Tremblay"), request.getId());

        assertEquals(GameRequestStatus.CONFIRMED, request.getStatus());
        assertFalse(slot.available());
        assertEquals(List.of(), service.getAvailableTimeSlots("Tennis", DAY));
        InvalidTimeSlotException e = assertThrows(InvalidTimeSlotException.class,
                () -> service.publishTimeSlot(FRANK, TENNIS_3, DAY, at("18:30"), at("19:30")));
        assertEquals(InvalidTimeSlotException.OVERLAPPING_SLOT_MESSAGE, e.getMessage());
    }
}
