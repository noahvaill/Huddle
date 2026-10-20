package ca.mcgill.ecse428.huddle;
 
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
/** Unit tests for the ID007 joining rules in GameRequestService.joinRequest (TID028). */
class GameRequestServiceJoinTest {
 
    private static final User ALICE = new User("Alice Chen");
    private static final User BOB = new User("Bob Tremblay");
    private static final User CARLA = new User("Carla Dubois");
    private static final User DAN = new User("Dan Lee");
    private static final User EVA = new User("Eva Roy");
 
    private GameRequestService service;
 
    private static TimeSlot slot(String court, Sport sport, int day, String start, String end) {
        return new TimeSlot(new Court(court, sport), LocalDate.of(2026, 10, day),
                LocalTime.parse(start), LocalTime.parse(end), true);
    }
 
    private GameRequest add(String id, User creator, TimeSlot slot, int groupSize, int spots,
                            GameRequestStatus status, List<User> participants) {
        GameRequest r = new GameRequest(id, creator, slot, groupSize, spots, status, participants);
        service.addRequest(r);
        return r;
    }
 
    private void assertJoinRejected(String expectedMessage, String requestId) {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.joinRequest(BOB, requestId));
        assertEquals(expectedMessage, e.getMessage());
    }
 
    @BeforeEach
    void setUp() {
        service = new GameRequestService();
    }
 
    @Test
    void joiningTheLastSpotAddsParticipantAndTakesTheSpot() {
        GameRequest r1 = add("R1", ALICE, slot("Squash Court 1", Sport.SQUASH, 15, "18:00", "18:45"),
                1, 1, GameRequestStatus.OPEN, List.of(ALICE));
 
        GameRequest result = service.joinRequest(BOB, "R1");
 
        assertSame(r1, result);
        assertEquals(List.of(ALICE, BOB), r1.getParticipants());
        assertEquals(0, r1.getSpotsRemaining());
    }
 
    @Test
    void joiningAGroupGameKeepsItOpenWithOneSpotLess() {
        GameRequest r2 = add("R2", CARLA, slot("Basketball Court", Sport.BASKETBALL, 15, "19:00", "20:00"),
                5, 5, GameRequestStatus.OPEN, List.of(CARLA));
 
        service.joinRequest(BOB, "R2");
 
        assertTrue(r2.hasParticipant(BOB));
        assertEquals(4, r2.getSpotsRemaining());
        assertEquals(GameRequestStatus.OPEN, r2.getStatus());
    }
 
    @Test
    void fullGameIsRejected() {
        GameRequest r3 = add("R3", ALICE, slot("Squash Court 1", Sport.SQUASH, 17, "18:00", "18:45"),
                1, 0, GameRequestStatus.CONFIRMED, List.of(ALICE, DAN));
 
        assertJoinRejected(InvalidGameRequestException.GAME_FULL_MESSAGE, "R3");
        assertFalse(r3.hasParticipant(BOB));
        assertEquals(0, r3.getSpotsRemaining());
    }
 
    @Test
    void joiningYourOwnRequestIsRejected() {
        GameRequest r4 = add("R4", BOB, slot("Squash Court 2", Sport.SQUASH, 18, "18:00", "18:45"),
                1, 1, GameRequestStatus.OPEN, List.of(BOB));
 
        assertJoinRejected(InvalidGameRequestException.OWN_REQUEST_MESSAGE, "R4");
        assertEquals(1, r4.getSpotsRemaining());
        assertEquals(List.of(BOB), r4.getParticipants());
    }
 
    @Test
    void joiningTwiceIsRejected() {
        GameRequest r5 = add("R5", DAN, slot("Badminton Court 2", Sport.BADMINTON, 19, "17:00", "18:00"),
                1, 2, GameRequestStatus.OPEN, List.of(DAN, BOB));
 
        assertJoinRejected(InvalidGameRequestException.ALREADY_JOINED_MESSAGE, "R5");
        assertEquals(2, r5.getSpotsRemaining());
        assertEquals(List.of(DAN, BOB), r5.getParticipants());
    }
 
    @Test
    void joiningAGameThatOverlapsAnotherOfYourGamesIsRejected() {
        add("R5", DAN, slot("Badminton Court 2", Sport.BADMINTON, 19, "17:00", "18:00"),
                1, 2, GameRequestStatus.OPEN, List.of(DAN, BOB));
        GameRequest r6 = add("R6", EVA, slot("Tennis Court 1", Sport.TENNIS, 19, "17:30", "18:30"),
                1, 1, GameRequestStatus.OPEN, List.of(EVA));
 
        assertJoinRejected(InvalidGameRequestException.OVERLAPPING_GAME_MESSAGE, "R6");
        assertFalse(r6.hasParticipant(BOB));
        assertEquals(1, r6.getSpotsRemaining());
    }
 
    @Test
    void backToBackGamesDoNotOverlap() {
        add("R1", DAN, slot("Squash Court 1", Sport.SQUASH, 15, "18:00", "18:45"),
                1, 0, GameRequestStatus.CONFIRMED, List.of(DAN, BOB));
        GameRequest r2 = add("R2", ALICE, slot("Squash Court 1", Sport.SQUASH, 15, "18:45", "19:30"),
                1, 1, GameRequestStatus.OPEN, List.of(ALICE));
 
        service.joinRequest(BOB, "R2");
 
        assertTrue(r2.hasParticipant(BOB));
    }
 
    @Test
    void sameTimeOnAnotherDayDoesNotOverlap() {
        add("R1", DAN, slot("Squash Court 1", Sport.SQUASH, 15, "18:00", "18:45"),
                1, 1, GameRequestStatus.OPEN, List.of(DAN, BOB));
        GameRequest r2 = add("R2", ALICE, slot("Squash Court 1", Sport.SQUASH, 16, "18:00", "18:45"),
                1, 1, GameRequestStatus.OPEN, List.of(ALICE));
 
        service.joinRequest(BOB, "R2");
 
        assertTrue(r2.hasParticipant(BOB));
    }
 
    @Test
    void cancelledGameDoesNotCountAsOverlap() {
        add("R1", DAN, slot("Squash Court 1", Sport.SQUASH, 15, "18:00", "18:45"),
                1, 1, GameRequestStatus.CANCELLED, List.of(DAN, BOB));
        GameRequest r2 = add("R2", ALICE, slot("Squash Court 2", Sport.SQUASH, 15, "18:00", "18:45"),
                1, 1, GameRequestStatus.OPEN, List.of(ALICE));
 
        service.joinRequest(BOB, "R2");
 
        assertTrue(r2.hasParticipant(BOB));
    }
 
    @Test
    void cancelledRequestIsRejected() {
        GameRequest r1 = add("R1", ALICE, slot("Squash Court 1", Sport.SQUASH, 15, "18:00", "18:45"),
                1, 1, GameRequestStatus.CANCELLED, List.of(ALICE));
 
        assertJoinRejected(InvalidGameRequestException.NOT_OPEN_MESSAGE, "R1");
        assertFalse(r1.hasParticipant(BOB));
    }
 
    @Test
    void unknownRequestIsRejected() {
        assertJoinRejected(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE, "R99");
    }
 
    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> service.joinRequest(null, "R1"));
        assertThrows(NullPointerException.class, () -> service.joinRequest(BOB, null));
    }
}
 
