package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for withdrawing from an open game, GameRequestService.withdrawFromRequest (ID008, TID042). */
class GameRequestServiceWithdrawTest {

    private static final User BOB = new User("Bob Tremblay");
    private static final User CARLA = new User("Carla Dubois");
    private static final User DAN = new User("Dan Lee");

    private static final TimeSlot BASKETBALL = new TimeSlot(new Court("Basketball Court", Sport.BASKETBALL),
            LocalDate.of(2026, 10, 15), LocalTime.of(19, 0), LocalTime.of(20, 0), true);

    private GameRequestService service;
    private GameRequest r2;

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
        // R2 from the ID008 feature: Carla's group game with Bob and Dan joined, 3 spots left
        r2 = new GameRequest("R2", CARLA, BASKETBALL, 5, 3, GameRequestStatus.OPEN, List.of(CARLA, BOB, DAN));
        service.addRequest(r2);
    }

    @Test
    void withdrawingRemovesParticipantAndFreesOneSpot() {
        GameRequest result = service.withdrawFromRequest(BOB, "R2");

        assertSame(r2, result);
        assertFalse(r2.hasParticipant(BOB));
        assertEquals(List.of(CARLA, DAN), r2.getParticipants());
        assertEquals(4, r2.getSpotsRemaining());
        assertEquals(GameRequestStatus.OPEN, r2.getStatus());
    }

    @Test
    void withdrawnUserCanJoinTheGameAgain() {
        service.withdrawFromRequest(BOB, "R2");
        service.joinRequest(BOB, "R2");

        assertEquals(List.of(CARLA, DAN, BOB), r2.getParticipants());
        assertEquals(3, r2.getSpotsRemaining());
    }

    @Test
    void unknownRequestIsRejected() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.withdrawFromRequest(BOB, "R9"));
        assertEquals(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE, e.getMessage());
    }

    @Test
    void creatorCannotBeRemovedAsAParticipant() {
        assertThrows(IllegalArgumentException.class, () -> r2.removeParticipant(CARLA));
        assertEquals(List.of(CARLA, BOB, DAN), r2.getParticipants());
    }

    @Test
    void removingANonParticipantIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> r2.removeParticipant(new User("Eva Roy")));
        assertEquals(3, r2.getSpotsRemaining());
    }
}