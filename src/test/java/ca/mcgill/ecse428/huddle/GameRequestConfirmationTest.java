package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID010: the game is confirmed and the court booked when the last spot is filled (TID032). */
class GameRequestConfirmationTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User BOB = new User("Bob Tremblay");
    private static final User CARLA = new User("Carla Dubois");

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
        assertEquals(2, r1.getParticipantCount());
        assertEquals("Game confirmed on Squash Court 1 on 2026-10-15 at 18:00",
                GameRequestService.confirmationMessage(r1));
    }

    @Test
    void lastPlayerJoiningGroupGameConfirmsItAndBooksTheSlot() {
        TimeSlot s3 = slot("Basketball Court", Sport.BASKETBALL, "19:00", "20:00");
        service.addRequest(new GameRequest("R2", CARLA, s3, 9, 1, GameRequestStatus.OPEN, List.of(CARLA)));

        GameRequest r2 = service.joinRequest(BOB, "R2");

        assertEquals(GameRequestStatus.CONFIRMED, r2.getStatus());
        assertFalse(s3.available());
        assertEquals(10, r2.getParticipantCount());
        assertEquals("Game confirmed on Basketball Court on 2026-10-15 at 19:00",
                GameRequestService.confirmationMessage(r2));
    }

    @Test
    void joinThatLeavesSpotsOpenDoesNotConfirmOrBook() {
        TimeSlot s3 = slot("Basketball Court", Sport.BASKETBALL, "19:00", "20:00");
        service.addRequest(new GameRequest("R2", CARLA, s3, 5, 5, GameRequestStatus.OPEN, List.of(CARLA)));

        GameRequest r2 = service.joinRequest(BOB, "R2");

        assertEquals(GameRequestStatus.OPEN, r2.getStatus());
        assertTrue(s3.available());
        assertEquals(4, r2.getSpotsRemaining());
    }

    @Test
    void postedRequestIsConfirmedWhenItsLastSpotIsFilled() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        GameRequest posted = service.postRequest(ALICE, s1, 1, 1);

        service.joinRequest(BOB, posted.getId());

        assertEquals(GameRequestStatus.CONFIRMED, posted.getStatus());
        assertFalse(s1.available());
    }
}