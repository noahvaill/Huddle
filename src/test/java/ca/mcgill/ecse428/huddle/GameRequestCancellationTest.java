package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID009: only the creator can cancel a game request. */
class GameRequestCancellationTest {

    private static final User CARLA = new User("Carla Dubois");
    private static final User ALICE = new User("Alice Chen");
    private GameRequestService service;

    private static TimeSlot slot() {
        return new TimeSlot(new Court("Badminton Court 1", Sport.BADMINTON), LocalDate.of(2026, 10, 18),
                LocalTime.of(18, 0), LocalTime.of(19, 0), true);
    }

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
        service.addRequest(new GameRequest("R3", CARLA, slot(), 1, 2, GameRequestStatus.OPEN, List.of(CARLA, ALICE)));
    }

    @Test
    void creatorCanCancel() {
        assertEquals(GameRequestStatus.CANCELLED, service.cancelRequest(CARLA, "R3").getStatus());
    }

    @Test
    void participantWhoIsNotTheCreatorCannotCancel() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.cancelRequest(ALICE, "R3"));

        assertEquals("Only the creator can cancel this game request", e.getMessage());
        assertEquals(GameRequestStatus.OPEN, service.findById("R3").orElseThrow().getStatus());
        assertTrue(service.getNotificationsFor(CARLA).isEmpty());
    }

    @Test
    void nonCreatorIsRejectedEvenIfTheRequestIsAlreadyCancelled() {
        service.cancelRequest(CARLA, "R3");

        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.cancelRequest(ALICE, "R3"));

        assertEquals("Only the creator can cancel this game request", e.getMessage());
    }

    @Test
    void unknownRequestIsRejectedBeforeTheCreatorCheck() {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.cancelRequest(ALICE, "R9"));

        assertEquals("Game request not found", e.getMessage());
    }
}
