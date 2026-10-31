package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for cancelling a game request, GameRequestService.cancelRequest (ID009, TID051-TID053).
 * The creator-only rule (TID054) is covered by GameRequestCancellationTest.
 */
class GameRequestServiceCancelTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User BOB = new User("Bob Tremblay");
    private static final User DAN = new User("Dan Lee");

    private GameRequestService service;
    private TimeSlot squash;
    private TimeSlot badminton;
    private GameRequest r1;
    private GameRequest r2;

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
        squash = new TimeSlot(new Court("Squash Court 1", Sport.SQUASH),
                LocalDate.of(2026, 10, 15), LocalTime.of(18, 0), LocalTime.of(18, 45), TimeSlotStatus.BOOKED);
        badminton = new TimeSlot(new Court("Badminton Court 2", Sport.BADMINTON),
                LocalDate.of(2026, 10, 16), LocalTime.of(17, 0), LocalTime.of(18, 0), TimeSlotStatus.AVAILABLE);
        // R1 and R2 from the ID009 feature: Alice's confirmed squash game with Bob, and her open badminton game with Dan
        r1 = new GameRequest("R1", ALICE, squash, 1, 0, GameRequestStatus.CONFIRMED, List.of(ALICE, BOB));
        r2 = new GameRequest("R2", ALICE, badminton, 1, 2, GameRequestStatus.OPEN, List.of(ALICE, DAN));
        service.addRequest(r1);
        service.addRequest(r2);
    }

    @Test
    void cancellingAnOpenRequestSetsItToCancelled() {
        GameRequest result = service.cancelRequest(ALICE, "R2");

        assertSame(r2, result);
        assertEquals(GameRequestStatus.CANCELLED, r2.getStatus());
        assertTrue(badminton.available());
    }

    @Test
    void cancellingAConfirmedRequestReleasesItsBookedSlot() {
        service.cancelRequest(ALICE, "R1");

        assertEquals(GameRequestStatus.CANCELLED, r1.getStatus());
        assertEquals(TimeSlotStatus.AVAILABLE, squash.status());
    }

    @Test
    void joinedParticipantsAreNotifiedButNotTheCreator() {
        service.cancelRequest(ALICE, "R2");

        assertEquals(List.of("The Badminton game on 2026-10-16 at 17:00 has been cancelled by the creator"),
                service.getNotificationsFor(DAN));
        assertTrue(service.getNotificationsFor(ALICE).isEmpty());
    }

    @Test
    void cancellingAConfirmedRequestNotifiesItsParticipants() {
        service.cancelRequest(ALICE, "R1");

        assertEquals(List.of("The Squash game on 2026-10-15 at 18:00 has been cancelled by the creator"),
                service.getNotificationsFor(BOB));
        assertTrue(service.getNotificationsFor(ALICE).isEmpty());
    }

    @Test
    void cancellingOneRequestLeavesTheOthersUnchanged() {
        service.cancelRequest(ALICE, "R2");

        assertEquals(GameRequestStatus.CONFIRMED, r1.getStatus());
        assertFalse(squash.available());
        assertTrue(service.getNotificationsFor(BOB).isEmpty());
    }

    @Test
    void cancelledRequestKeepsItsParticipants() {
        service.cancelRequest(ALICE, "R2");

        assertEquals(List.of(ALICE, DAN), r2.getParticipants());
        assertSame(r2, service.findById("R2").orElseThrow());
    }

    @Test
    void cancellingTwiceSendsNoNewNotifications() {
        service.cancelRequest(ALICE, "R1");
        GameRequest result = service.cancelRequest(ALICE, "R1");

        assertSame(r1, result);
        assertEquals(GameRequestStatus.CANCELLED, r1.getStatus());
        assertEquals(1, service.getNotificationsFor(BOB).size());
        assertTrue(squash.available());
    }

    @Test
    void unknownRequestIsRejected() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.cancelRequest(ALICE, "R9"));
        assertEquals(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE, e.getMessage());
    }

    @Test
    void missingUserOrRequestIdIsRejected() {
        assertThrows(NullPointerException.class, () -> service.cancelRequest(null, "R2"));
        assertThrows(NullPointerException.class, () -> service.cancelRequest(ALICE, null));
        assertEquals(GameRequestStatus.OPEN, r2.getStatus());
    }
}
