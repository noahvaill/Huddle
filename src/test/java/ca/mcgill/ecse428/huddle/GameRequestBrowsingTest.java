package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class GameRequestBrowsingTest {

    private static final User BOB = new User("Bob Tremblay");
    private static final User ALICE = new User("Alice Chen");
    private static final User CARLA = new User("Carla Dubois");
    private GameRequestService service;

    private static GameRequest request(String id, User creator, Sport sport, String court, String date,
                                       String start, String end, int spots, GameRequestStatus status) {
        TimeSlot slot = new TimeSlot(new Court(court, sport), LocalDate.parse(date),
                LocalTime.parse(start), LocalTime.parse(end), status != GameRequestStatus.CONFIRMED);
        return new GameRequest(id, creator, slot, 1, spots, status, List.of(creator));
    }

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
        service.addRequest(request("R1", ALICE, Sport.SQUASH, "Squash Court 1", "2026-10-15",
                "18:00", "18:45", 1, GameRequestStatus.OPEN));
        service.addRequest(request("R2", CARLA, Sport.SQUASH, "Squash Court 2", "2026-10-16",
                "19:30", "20:15", 1, GameRequestStatus.OPEN));
        service.addRequest(request("R3", ALICE, Sport.SQUASH, "Squash Court 1", "2026-10-17",
                "18:00", "18:45", 0, GameRequestStatus.CONFIRMED));
        service.addRequest(request("R4", CARLA, Sport.BADMINTON, "Badminton Court 2", "2026-10-15",
                "17:00", "18:00", 2, GameRequestStatus.OPEN));
        service.addRequest(request("R5", BOB, Sport.SQUASH, "Squash Court 2", "2026-10-18",
                "18:00", "18:45", 1, GameRequestStatus.OPEN));
    }

    private List<String> ids(User user, String sport) {
        return service.getOpenGameRequests(user, sport).stream().map(GameRequest::getId).toList();
    }

    @Test
    void listsOnlyOtherUsersOpenRequestsForSelectedSport() {
        List<GameRequest> requests = service.getOpenGameRequests(BOB, "Squash");

        assertEquals(List.of("R1", "R2"), requests.stream().map(GameRequest::getId).toList());
        assertEquals(List.of(ALICE, CARLA), requests.stream().map(GameRequest::getCreator).toList());
        assertEquals(List.of("Squash Court 1", "Squash Court 2"),
                requests.stream().map(r -> r.getTimeSlot().court().name()).toList());
        assertEquals(List.of(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 16)),
                requests.stream().map(r -> r.getTimeSlot().date()).toList());
        assertEquals(List.of(LocalTime.of(18, 0), LocalTime.of(19, 30)),
                requests.stream().map(r -> r.getTimeSlot().start()).toList());
        assertEquals(List.of(LocalTime.of(18, 45), LocalTime.of(20, 15)),
                requests.stream().map(r -> r.getTimeSlot().end()).toList());
        assertEquals(List.of(1, 1), requests.stream().map(GameRequest::getSpotsRemaining).toList());
        assertEquals(List.of("R4"), ids(BOB, "Badminton"));
    }

    @Test
    void offeredSportWithNoRequestsReturnsEmptyList() {
        assertTrue(service.getOpenGameRequests(BOB, "Tennis").isEmpty());
        assertEquals("No open game requests", GameRequestService.NO_OPEN_REQUESTS_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Curling", "   "})
    void unsupportedSportIsRejected(String sport) {
        SportNotOfferedException error = assertThrows(SportNotOfferedException.class,
                () -> service.getOpenGameRequests(BOB, sport));

        assertEquals("Sport not offered", error.getMessage());
    }

    @Test
    void emptyServiceReturnsEmptyListForOfferedSport() {
        assertTrue(new GameRequestService().getOpenGameRequests(BOB, "Squash").isEmpty());
    }

    @Test
    void emptyServiceStillValidatesSport() {
        assertThrows(SportNotOfferedException.class,
                () -> new GameRequestService().getOpenGameRequests(BOB, "Curling"));
    }

    @Test
    void cancelledRequestsAreExcluded() {
        service.findById("R1").orElseThrow().setStatus(GameRequestStatus.CANCELLED);

        assertEquals(List.of("R2"), ids(BOB, "Squash"));
    }

    @Test
    void equalUserValuesExcludeOwnRequests() {
        assertEquals(List.of("R1", "R2"), ids(new User(" Bob Tremblay "), "Squash"));
        assertEquals(List.of("R2", "R5"), ids(new User("Alice Chen"), "Squash"));
    }

    @Test
    void sportNamesIgnoreCaseAndSurroundingWhitespace() {
        assertEquals(List.of("R1", "R2"), ids(BOB, "  sQuAsH  "));
    }

    @Test
    void browsingPreservesInsertionOrderRatherThanSortingByDate() {
        service.addRequest(request("R6", ALICE, Sport.SQUASH, "Squash Court 1", "2026-10-14",
                "17:00", "17:45", 1, GameRequestStatus.OPEN));

        assertEquals(List.of("R1", "R2", "R6"), ids(BOB, "Squash"));
    }

    @Test
    void browsingDoesNotFilterByAvailabilityOrParticipation() {
        GameRequest existing = service.findById("R1").orElseThrow();
        existing.addParticipant(BOB);
        TimeSlot slot = existing.getTimeSlot();
        TimeSlot booked = new TimeSlot(slot.court(), slot.date(), slot.start(), slot.end(), false);
        service.addRequest(new GameRequest("R6", CARLA, booked, 1, 1));

        assertEquals(List.of("R1", "R2", "R6"), ids(BOB, "Squash"));
    }

    @Test
    void repeatedBrowsingDoesNotChangeStoredRequests() {
        List<GameRequest> before = service.getRequests();
        List<List<User>> participants = before.stream().map(GameRequest::getParticipants).toList();
        List<GameRequestStatus> statuses = before.stream().map(GameRequest::getStatus).toList();
        List<Integer> spots = before.stream().map(GameRequest::getSpotsRemaining).toList();

        assertEquals(List.of("R1", "R2"), ids(BOB, "Squash"));
        assertEquals(List.of("R1", "R2"), ids(BOB, "Squash"));
        service.getOpenGameRequests(BOB, "Tennis");
        assertThrows(SportNotOfferedException.class, () -> service.getOpenGameRequests(BOB, "Curling"));

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
    void returnedListIsUnmodifiable() {
        List<GameRequest> result = service.getOpenGameRequests(BOB, "Squash");

        assertThrows(UnsupportedOperationException.class, result::clear);
        assertEquals(5, service.getRequests().size());
        assertEquals(List.of("R1", "R2"), ids(BOB, "Squash"));
    }

    @Test
    void currentUserIsRequired() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> service.getOpenGameRequests(null, "Squash"));

        assertEquals("User is required", error.getMessage());
    }
}
