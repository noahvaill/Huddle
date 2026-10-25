package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class GameRequestServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final User ALICE = new User("Alice Chen");
    private GameRequestService service;

    private static TimeSlot slot(String court, Sport sport, String start, String end, boolean available) {
        return new TimeSlot(new Court(court, sport), DAY, LocalTime.parse(start), LocalTime.parse(end), available);
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
        assertRejected(ALICE, expectedMessage, slot, groupSize, playersNeeded);
    }

    private void assertRejected(User creator, String expectedMessage, TimeSlot slot, int groupSize, int playersNeeded) {
        List<GameRequest> before = service.getRequests();
        List<List<User>> participants = before.stream().map(GameRequest::getParticipants).toList();
        List<GameRequestStatus> statuses = before.stream().map(GameRequest::getStatus).toList();
        List<Integer> spots = before.stream().map(GameRequest::getSpotsRemaining).toList();
        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.postRequest(creator, slot, groupSize, playersNeeded));
        assertEquals(expectedMessage, e.getMessage());
        List<GameRequest> after = service.getRequests();
        assertEquals(before, after);
        for (int i = 0; i < before.size(); i++) {
            assertSame(before.get(i), after.get(i));
        }
        assertEquals(participants, after.stream().map(GameRequest::getParticipants).toList());
        assertEquals(statuses, after.stream().map(GameRequest::getStatus).toList());
        assertEquals(spots, after.stream().map(GameRequest::getSpotsRemaining).toList());
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

    @ParameterizedTest
    @CsvSource({
            "17:45, 18:15", // overlaps the beginning
            "18:30, 19:00", // overlaps the end
            "18:00, 18:45", // identical interval
            "18:15, 18:30", // contained by the existing game
            "17:45, 19:00"  // contains the existing game
    })
    void overlappingIntervalsAreRejected(String start, String end) {
        service.postRequest(ALICE, SQUASH, 1, 1);
        TimeSlot candidate = slot("Squash Court 1", Sport.SQUASH, start, end, true);

        assertRejected("You already have a game at this time", candidate, 1, 1);
    }

    @ParameterizedTest
    @CsvSource({"17:15, 18:00", "18:45, 19:30"})
    void touchingEndpointsAreAccepted(String start, String end) {
        service.postRequest(ALICE, SQUASH, 1, 1);
        TimeSlot candidate = slot("Squash Court 1", Sport.SQUASH, start, end, true);

        GameRequest request = service.postRequest(ALICE, candidate, 1, 1);

        assertEquals(candidate, request.getTimeSlot());
        assertEquals(2, service.getRequests().size());
    }

    @ParameterizedTest
    @EnumSource(value = GameRequestStatus.class, names = {"OPEN", "CONFIRMED"})
    void activeGamesBlockOverlaps(GameRequestStatus status) {
        service.addRequest(new GameRequest("R1", ALICE, SQUASH, 1,
                status == GameRequestStatus.CONFIRMED ? 0 : 1, status, List.of(ALICE)));

        assertRejected("You already have a game at this time", SQUASH, 1, 1);
    }

    @Test
    void cancelledGamesDoNotBlockOverlaps() {
        service.addRequest(new GameRequest("R1", ALICE, SQUASH, 1, 1,
                GameRequestStatus.CANCELLED, List.of(ALICE)));

        GameRequest request = service.postRequest(ALICE, SQUASH, 1, 1);

        assertEquals(GameRequestStatus.OPEN, request.getStatus());
        assertEquals(2, service.getRequests().size());
    }

    @Test
    void joinedGameBlocksPostingAnOverlap() {
        GameRequest existing = service.postRequest(new User("Bob Tremblay"), BADMINTON, 1, 2);
        service.joinRequest(ALICE, existing.getId());

        assertRejected("You already have a game at this time", SQUASH, 1, 1);
    }

    @Test
    void sameTimesOnDifferentDatesAreAccepted() {
        service.postRequest(ALICE, SQUASH, 1, 1);
        TimeSlot tomorrow = new TimeSlot(SQUASH.court(), DAY.plusDays(1),
                SQUASH.start(), SQUASH.end(), true);

        assertEquals(tomorrow, service.postRequest(ALICE, tomorrow, 1, 1).getTimeSlot());
    }

    @Test
    void anotherUsersGameDoesNotBlockPosting() {
        service.postRequest(new User("Bob Tremblay"), SQUASH, 1, 1);

        assertEquals(ALICE, service.postRequest(ALICE, SQUASH, 1, 1).getCreator());
    }

    @Test
    void equalUsersCannotPostOverlappingGames() {
        service.postRequest(ALICE, SQUASH, 1, 1);

        assertRejected(new User("Alice Chen"), "You already have a game at this time", SQUASH, 1, 1);
    }

    @Test
    void overlapOnAnotherCourtIsRejected() {
        service.postRequest(ALICE, SQUASH, 1, 1);
        TimeSlot otherCourt = slot("Squash Court 2", Sport.SQUASH, "18:00", "18:45", true);

        assertRejected("You already have a game at this time", otherCourt, 1, 1);
    }

    @Test
    void needingNegativePlayersIsRejected() {
        assertRejected("At least 1 more player is required", SQUASH, 1, -1);
    }

    @Test
    void exactCapacityIsAccepted() {
        GameRequest request = service.postRequest(ALICE, BADMINTON, 2, 2);

        assertEquals(service.getMaxPlayers(Sport.BADMINTON),
                request.getGroupSize() + request.getSpotsRemaining());
        assertEquals(GameRequestStatus.OPEN, request.getStatus());
    }

    @Test
    void unavailableSlotIsCheckedBeforePlayerCountAndOverlap() {
        service.postRequest(ALICE, BADMINTON, 1, 1);
        TimeSlot unavailable = new TimeSlot(SQUASH.court(), DAY, SQUASH.start(), SQUASH.end(), false);

        assertRejected("Time slot is not available", unavailable, 1, 0);
    }

    @Test
    void invalidPlayerCountIsCheckedBeforeCapacityAndOverlap() {
        service.postRequest(ALICE, BADMINTON, 1, 1);

        assertRejected("At least 1 more player is required", SQUASH, 3, 0);
    }

    @Test
    void capacityIsCheckedBeforeOverlap() {
        service.postRequest(ALICE, BADMINTON, 1, 1);

        assertRejected("Squash allows at most 1 more player", SQUASH, 1, 2);
    }
}
