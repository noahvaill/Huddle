package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameRequestServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final User ALICE = new User("Alice Chen");
    private GameRequestService service;

    private static TimeSlot slot(String court, Sport sport, String start, String end, boolean available) {
        return new TimeSlot(court, sport, DAY, LocalTime.parse(start), LocalTime.parse(end), available);
    }

    private static final TimeSlot SQUASH = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45", true);
    private static final TimeSlot BOOKED = slot("Squash Court 1", Sport.SQUASH, "18:45", "19:30", false);
    private static final TimeSlot BASKETBALL = slot("Basketball Court", Sport.BASKETBALL, "19:00", "20:00", true);
    private static final TimeSlot BADMINTON = slot("Badminton Court 2", Sport.BADMINTON, "18:00", "19:00", true);

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
    }

    private void assertRejected(String expectedMessage, TimeSlot slot, int groupSize, int playersNeeded) {
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.postRequest(ALICE, slot, groupSize, playersNeeded));
        assertEquals(expectedMessage, e.getMessage());
        assertTrue(service.findByTimeSlot(slot).isEmpty());
    }

    @Test
    void postsOpenRequestForOnePartner() {
        GameRequest r = service.postRequest(ALICE, SQUASH, 1, 1);

        assertEquals("R1", r.getId());
        assertEquals(ALICE, r.getCreator());
        assertEquals(SQUASH, r.getTimeSlot());
        assertEquals(GameRequestStatus.OPEN, r.getStatus());
        assertEquals(1, r.getSpotsRemaining());
        assertEquals(List.of(ALICE), r.getParticipants());
        assertEquals(r, service.findByTimeSlot(SQUASH).orElseThrow());
    }

    @Test
    void groupOfFiveCanLookForFiveMoreOnBasketball() {
        GameRequest r = service.postRequest(ALICE, BASKETBALL, 5, 5);

        assertEquals(5, r.getGroupSize());
        assertEquals(5, r.getSpotsRemaining());
        assertEquals(GameRequestStatus.OPEN, r.getStatus());
    }

    @Test
    void bookedSlotIsRejected() {
        assertRejected("Time slot is not available", BOOKED, 1, 1);
    }

    @Test
    void needingZeroPlayersIsRejected() {
        assertRejected("At least 1 more player is required", SQUASH, 1, 0);
    }

    @Test
    void tooManyPlayersIsRejectedWithSingularMessage() {
        assertRejected("Squash allows at most 1 more player", SQUASH, 1, 3);
    }

    @Test
    void tooManyPlayersIsRejectedWithPluralMessage() {
        assertRejected("Badminton allows at most 3 more players", BADMINTON, 1, 5);
    }

    @Test
    void maxPlayersCanBeChanged() {
        service.setMaxPlayers(Sport.SQUASH, 4);
        assertEquals(4, service.getMaxPlayers(Sport.SQUASH));
        assertEquals(3, service.postRequest(ALICE, SQUASH, 1, 3).getSpotsRemaining());
    }

    @Test
    void overlappingGameOfSameUserIsRejected() {
        service.postRequest(ALICE, BADMINTON, 1, 1);
        assertRejected("You already have a game at this time", SQUASH, 1, 1);
    }

    @Test
    void nonOverlappingSlotOnSameDayIsAccepted() {
        service.postRequest(ALICE, SQUASH, 1, 1);
        GameRequest r = service.postRequest(ALICE, BASKETBALL, 1, 1);
        assertEquals(GameRequestStatus.OPEN, r.getStatus());
    }

    @Test
    void idsIncrementInCreationOrder() {
        GameRequest first = service.postRequest(ALICE, SQUASH, 1, 1);
        GameRequest second = service.postRequest(new User("Bob Martin"), BADMINTON, 1, 1);

        assertEquals("R1", first.getId());
        assertEquals("R2", second.getId());
        assertEquals(List.of(first, second), service.getRequests());
        assertEquals(second, service.findById("R2").orElseThrow());
    }
}
