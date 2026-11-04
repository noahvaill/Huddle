package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID008: the rules for withdrawing from a game, GameRequestService.withdrawFromRequest (TID046). */
class GameRequestWithdrawRulesTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User BOB = new User("Bob Tremblay");
    private static final User CARLA = new User("Carla Dubois");
    private static final User DAN = new User("Dan Lee");
    private static final User EVA = new User("Eva Roy");

    private GameRequestService service;
    private TimeSlot s1;
    private TimeSlot s2;
    private GameRequest r1;
    private GameRequest r2;
    private GameRequest r3;
    private GameRequest r5;

    private static TimeSlot slot(String court, Sport sport, String date, String start, String end, boolean available) {
        return new TimeSlot(new Court(court, sport), LocalDate.parse(date),
                LocalTime.parse(start), LocalTime.parse(end), available);
    }

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
        // R1, R2, R3 and R5 from the ID008 feature background
        s1 = slot("Squash Court", Sport.SQUASH, "2026-10-15", "18:00", "18:45", false);
        s2 = slot("Basketball Court", Sport.BASKETBALL, "2026-10-15", "19:00", "20:00", true);
        r1 = new GameRequest("R1", ALICE, s1, 1, 0, GameRequestStatus.CONFIRMED, List.of(ALICE, BOB));
        r2 = new GameRequest("R2", CARLA, s2, 5, 3, GameRequestStatus.OPEN, List.of(CARLA, BOB, DAN));
        r3 = new GameRequest("R3", CARLA, slot("Badminton Court", Sport.BADMINTON, "2026-10-16", "17:00", "18:00", true),
                1, 2, GameRequestStatus.OPEN, List.of(CARLA, DAN));
        r5 = new GameRequest("R5", BOB, slot("Squash Court", Sport.SQUASH, "2026-10-18", "18:00", "18:45", true),
                1, 1, GameRequestStatus.OPEN, List.of(BOB));
        for (GameRequest r : List.of(r1, r2, r3, r5)) {
            service.addRequest(r);
        }
    }

    // Error rules: each rejected withdraw leaves the request unchanged

    @Test
    void creatorCannotWithdrawFromTheirOwnGame() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.withdrawFromRequest(BOB, "R5"));

        assertEquals(InvalidGameRequestException.CREATOR_CANNOT_WITHDRAW_MESSAGE, e.getMessage());
        assertEquals(List.of(BOB), r5.getParticipants());
        assertEquals(1, r5.getSpotsRemaining());
        assertEquals(GameRequestStatus.OPEN, r5.getStatus());
    }

    @Test
    void creatorCannotWithdrawFromTheirConfirmedGame() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.withdrawFromRequest(ALICE, "R1"));

        assertEquals(InvalidGameRequestException.CREATOR_CANNOT_WITHDRAW_MESSAGE, e.getMessage());
        assertEquals(GameRequestStatus.CONFIRMED, r1.getStatus());
        assertEquals(0, r1.getSpotsRemaining());
        assertFalse(s1.available());
    }

    @Test
    void userWhoHasNotJoinedCannotWithdraw() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.withdrawFromRequest(BOB, "R3"));

        assertEquals(InvalidGameRequestException.NOT_A_PARTICIPANT_MESSAGE, e.getMessage());
        assertEquals(List.of(CARLA, DAN), r3.getParticipants());
        assertEquals(2, r3.getSpotsRemaining());
    }

    @Test
    void withdrawingTwiceIsRejectedTheSecondTime() {
        service.withdrawFromRequest(BOB, "R2");

        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.withdrawFromRequest(BOB, "R2"));

        assertEquals(InvalidGameRequestException.NOT_A_PARTICIPANT_MESSAGE, e.getMessage());
        assertEquals(4, r2.getSpotsRemaining());
    }

    @Test
    void withdrawingFromUnknownRequestIsRejected() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.withdrawFromRequest(BOB, "R9"));

        assertEquals(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE, e.getMessage());
    }

    @Test
    void nullUserIsRejected() {
        assertThrows(NullPointerException.class, () -> service.withdrawFromRequest(null, "R2"));
        assertEquals(3, r2.getSpotsRemaining());
    }

    @Test
    void nullRequestIdIsRejected() {
        assertThrows(NullPointerException.class, () -> service.withdrawFromRequest(BOB, null));
    }

    // Confirmed game rules

    @Test
    void withdrawingFromConfirmedGameReopensItAndReleasesTheSlot() {
        GameRequest result = service.withdrawFromRequest(BOB, "R1");

        assertEquals(GameRequestStatus.OPEN, result.getStatus());
        assertEquals(1, result.getSpotsRemaining());
        assertEquals(List.of(ALICE), result.getParticipants());
        assertEquals(TimeSlotStatus.AVAILABLE, s1.status());
    }

    @Test
    void reopenedGameIsConfirmedAgainWhenItsLastSpotIsFilled() {
        service.withdrawFromRequest(BOB, "R1");

        service.joinRequest(EVA, "R1");

        assertEquals(GameRequestStatus.CONFIRMED, r1.getStatus());
        assertEquals(0, r1.getSpotsRemaining());
        assertEquals(List.of(ALICE, EVA), r1.getParticipants());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
    }

    // Side effects

    @Test
    void withdrawingFromOpenGameKeepsItOpenAndTheSlotAvailable() {
        service.withdrawFromRequest(BOB, "R2");

        assertEquals(GameRequestStatus.OPEN, r2.getStatus());
        assertTrue(s2.available());
    }

    @Test
    void withdrawnUserCanJoinAnotherGameAtAnOverlappingTime() {
        GameRequest r6 = new GameRequest("R6", EVA, slot("Tennis Court", Sport.TENNIS, "2026-10-15", "18:00", "19:00", true),
                1, 1, GameRequestStatus.OPEN, List.of(EVA));
        service.addRequest(r6);
        assertThrows(InvalidGameRequestException.class, () -> service.joinRequest(BOB, "R6"));

        service.withdrawFromRequest(BOB, "R1");
        service.joinRequest(BOB, "R6");

        assertTrue(r6.hasParticipant(BOB));
    }

    @Test
    void withdrawingSendsNoNotifications() {
        service.withdrawFromRequest(BOB, "R1");

        assertTrue(service.getNotificationsFor(ALICE).isEmpty());
        assertTrue(service.getNotificationsFor(BOB).isEmpty());
    }
}
