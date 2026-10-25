package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** ID010: unit tests for game confirmation, court booking and cancellation (TID036). */
class GameRequestConfirmationTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User BOB = new User("Bob Tremblay");
    private static final User CARLA = new User("Carla Dubois");
    private static final User DAN = new User("Dan Lee");

    private GameRequestService service;

    private static TimeSlot slot(String court, Sport sport, String start, String end) {
        return new TimeSlot(new Court(court, sport), LocalDate.of(2026, 10, 15),
                LocalTime.parse(start), LocalTime.parse(end), true);
    }

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
    }

    @Test
    void lastPlayerJoiningTwoPlayerGameConfirmsItAndBooksTheSlot() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        service.addRequest(new GameRequest("R1", ALICE, s1, 1, 1, GameRequestStatus.OPEN, List.of(ALICE)));

        GameRequest r1 = service.joinRequest(BOB, "R1");

        assertEquals(GameRequestStatus.CONFIRMED, r1.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertFalse(s1.available());
        assertEquals(List.of(ALICE, BOB), r1.getParticipants());
        assertEquals(2, r1.getParticipantCount());
        assertEquals(0, r1.getSpotsRemaining());
        assertEquals("Game confirmed on Squash Court 1 on 2026-10-15 at 18:00",
                GameRequestService.confirmationMessage(r1));
    }

    @Test
    void lastPlayerJoiningGroupGameConfirmsItAndBooksTheSlot() {
        TimeSlot s3 = slot("Basketball Court", Sport.BASKETBALL, "19:00", "20:00");
        service.addRequest(new GameRequest("R2", CARLA, s3, 9, 1, GameRequestStatus.OPEN, List.of(CARLA)));

        GameRequest r2 = service.joinRequest(BOB, "R2");

        assertEquals(GameRequestStatus.CONFIRMED, r2.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s3.status());
        assertFalse(s3.available());
        assertEquals(List.of(CARLA, BOB), r2.getParticipants());
        assertEquals(10, r2.getParticipantCount());
        assertEquals(0, r2.getSpotsRemaining());
        assertEquals("Game confirmed on Basketball Court on 2026-10-15 at 19:00",
                GameRequestService.confirmationMessage(r2));
    }

    @Test
    void joinThatLeavesSpotsOpenDoesNotConfirmOrBook() {
        TimeSlot s3 = slot("Basketball Court", Sport.BASKETBALL, "19:00", "20:00");
        service.addRequest(new GameRequest("R2", CARLA, s3, 5, 5, GameRequestStatus.OPEN, List.of(CARLA)));

        GameRequest r2 = service.joinRequest(BOB, "R2");

        assertEquals(GameRequestStatus.OPEN, r2.getStatus());
        assertEquals(TimeSlotStatus.AVAILABLE, s3.status());
        assertTrue(s3.available());
        assertEquals(List.of(CARLA, BOB), r2.getParticipants());
        assertEquals(6, r2.getParticipantCount());
        assertEquals(4, r2.getSpotsRemaining());
    }

    @Test
    void postedRequestIsConfirmedWhenItsLastSpotIsFilled() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        GameRequest posted = service.postRequest(ALICE, s1, 1, 1);

        service.joinRequest(BOB, posted.getId());

        assertEquals(GameRequestStatus.CONFIRMED, posted.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertFalse(s1.available());
        assertEquals(List.of(ALICE, BOB), posted.getParticipants());
        assertEquals(0, posted.getSpotsRemaining());
    }

    @Test
    void successiveJoinsOnlyConfirmAndBookWhenTheLastSpotIsFilled() {
        TimeSlot s1 = slot("Badminton Court 1", Sport.BADMINTON, "18:00", "19:00");
        GameRequest request = service.postRequest(ALICE, s1, 1, 3);

        for (User user : List.of(BOB, CARLA)) {
            service.joinRequest(user, request.getId());

            assertEquals(GameRequestStatus.OPEN, request.getStatus());
            assertEquals(TimeSlotStatus.AVAILABLE, s1.status());
            assertTrue(s1.available());
            assertTrue(request.hasParticipant(user));
            assertEquals(4 - request.getParticipantCount(), request.getSpotsRemaining());
        }

        service.joinRequest(DAN, request.getId());

        assertEquals(GameRequestStatus.CONFIRMED, request.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertFalse(s1.available());
        assertEquals(List.of(ALICE, BOB, CARLA, DAN), request.getParticipants());
        assertEquals(4, request.getParticipantCount());
        assertEquals(0, request.getSpotsRemaining());
    }

    @Test
    void confirmedSlotIsRemovedFromAvailableListingsWithoutAffectingOtherSlots() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        TimeSlot s2 = slot("Squash Court 1", Sport.SQUASH, "18:45", "19:30");
        TimeSlotService timeSlots = new TimeSlotService();
        timeSlots.addTimeSlot(s1);
        timeSlots.addTimeSlot(s2);
        GameRequest request = service.postRequest(ALICE, s1, 1, 1);
        assertEquals(List.of(s1, s2), timeSlots.getAvailableTimeSlots("Squash", s1.date()));

        service.joinRequest(BOB, request.getId());

        assertEquals(List.of(s2), timeSlots.getAvailableTimeSlots("Squash", s1.date()));
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertEquals(TimeSlotStatus.AVAILABLE, s2.status());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void unavailableSlotCancelsTheGameAndNotifiesOnlyTheCreatorOnce(int spotsRemaining) {
        TimeSlot s1 = slot("Badminton Court 1", Sport.BADMINTON, "18:00", "19:00");
        int groupSize = 4 - spotsRemaining;
        GameRequest request = service.postRequest(ALICE, s1, groupSize, spotsRemaining);
        s1.book(); // Another booking occurs after posting, before the join starts.

        InvalidGameRequestException error = assertThrows(InvalidGameRequestException.class,
                () -> service.joinRequest(BOB, request.getId()));

        assertEquals("Time slot is no longer available", error.getMessage());
        assertEquals(GameRequestStatus.CANCELLED, request.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertEquals(List.of(ALICE), request.getParticipants());
        assertEquals(groupSize, request.getParticipantCount());
        assertEquals(spotsRemaining, request.getSpotsRemaining());
        List<String> expectedNotifications = List.of(
                "The Badminton game on 2026-10-15 at 18:00 has been cancelled: time slot no longer available");
        assertEquals(expectedNotifications, service.getNotificationsFor(ALICE));
        assertTrue(service.getNotificationsFor(BOB).isEmpty());

        InvalidGameRequestException retryError = assertThrows(InvalidGameRequestException.class,
                () -> service.joinRequest(BOB, request.getId()));

        assertEquals("This game is no longer open", retryError.getMessage());
        assertEquals(GameRequestStatus.CANCELLED, request.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertEquals(List.of(ALICE), request.getParticipants());
        assertEquals(groupSize, request.getParticipantCount());
        assertEquals(spotsRemaining, request.getSpotsRemaining());
        assertEquals(expectedNotifications, service.getNotificationsFor(ALICE));
        assertTrue(service.getNotificationsFor(BOB).isEmpty());
    }

    @Test
    void furtherJoinCannotChangeAConfirmedGameOrItsBooking() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        GameRequest request = service.postRequest(ALICE, s1, 1, 1);
        service.joinRequest(BOB, request.getId());

        InvalidGameRequestException error = assertThrows(InvalidGameRequestException.class,
                () -> service.joinRequest(CARLA, request.getId()));

        assertEquals("This game is full", error.getMessage());
        assertEquals(GameRequestStatus.CONFIRMED, request.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertEquals(List.of(ALICE, BOB), request.getParticipants());
        assertEquals(2, request.getParticipantCount());
        assertEquals(0, request.getSpotsRemaining());
        assertTrue(service.getNotificationsFor(ALICE).isEmpty());
        assertTrue(service.getNotificationsFor(CARLA).isEmpty());
    }
}
