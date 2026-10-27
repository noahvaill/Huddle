package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Focused cancellation and browse tests for ID009, TID051. */
class GameRequestServiceCancelTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User BOB = new User("Bob Tremblay");
    private static final User CARLA = new User("Carla Dubois");

    private GameRequestService service;
    private TimeSlot slot;
    private GameRequest request;

    @BeforeEach
    void setUp() {
        service = new GameRequestService();
        slot = new TimeSlot(new Court("Basketball Court", Sport.BASKETBALL),
                LocalDate.of(2026, 10, 29), LocalTime.of(19, 0), LocalTime.of(20, 0), true);
        request = new GameRequest("R1", ALICE, slot, 5, 4,
                GameRequestStatus.OPEN, List.of(ALICE, BOB));
        service.addRequest(request);
    }

    @Test
    void cancellingAnOpenRequestReturnsTheSameRequestWithCancelledStatus() {
        GameRequest result = service.cancelRequest(ALICE, "R1");

        assertSame(request, result);
        assertEquals(GameRequestStatus.CANCELLED, request.getStatus());
    }

    @Test
    void cancellingHidesTheRequestFromBrowseAndKeepsOtherOpenRequestsVisible() {
        TimeSlot otherSlot = new TimeSlot(slot.court(), slot.date().plusDays(1),
                slot.start(), slot.end(), true);
        GameRequest other = service.postRequest(BOB, otherSlot, 1, 2);
        assertEquals(List.of(request, other), service.getOpenGameRequests(CARLA, "Basketball"));

        service.cancelRequest(ALICE, "R1");

        assertEquals(List.of(other), service.getOpenGameRequests(CARLA, "Basketball"));
        assertEquals(GameRequestStatus.OPEN, other.getStatus());
    }

    @Test
    void cancellingAConfirmedRequestChangesItsStatusToCancelled() {
        TimeSlot confirmedSlot = new TimeSlot(new Court("Squash Court 1", Sport.SQUASH),
                slot.date().plusDays(1), slot.start(), slot.end(), true);
        GameRequest confirmed = service.postRequest(CARLA, confirmedSlot, 1, 1);
        service.joinRequest(BOB, confirmed.getId());
        assertEquals(GameRequestStatus.CONFIRMED, confirmed.getStatus());

        GameRequest result = service.cancelRequest(CARLA, confirmed.getId());

        assertSame(confirmed, result);
        assertEquals(GameRequestStatus.CANCELLED, confirmed.getStatus());
    }

    @Test
    void repeatedCancellationReturnsTheRequestUnchanged() {
        service.cancelRequest(ALICE, "R1");

        GameRequest result = service.cancelRequest(ALICE, "R1");

        assertSame(request, result);
        assertEquals(GameRequestStatus.CANCELLED, request.getStatus());
        assertRequestDetailsRetained();
    }

    @Test
    void cancellingRetainsTheStoredRequestAndItsDetails() {
        service.cancelRequest(ALICE, "R1");

        assertRequestDetailsRetained();
    }

    @Test
    void unknownRequestIsRejectedWithoutChangingStoredRequests() {
        InvalidGameRequestException error = assertThrows(InvalidGameRequestException.class,
                () -> service.cancelRequest(ALICE, "R9"));

        assertEquals(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE, error.getMessage());
        assertEquals(GameRequestStatus.OPEN, request.getStatus());
        assertRequestDetailsRetained();
    }

    @Test
    void nullUserIsRejectedWithoutChangingStoredRequests() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> service.cancelRequest(null, "R1"));

        assertEquals("User is required", error.getMessage());
        assertEquals(GameRequestStatus.OPEN, request.getStatus());
        assertRequestDetailsRetained();
    }

    @Test
    void nullRequestIdIsRejectedWithoutChangingStoredRequests() {
        NullPointerException error = assertThrows(NullPointerException.class,
                () -> service.cancelRequest(ALICE, null));

        assertEquals("Game request id is required", error.getMessage());
        assertEquals(GameRequestStatus.OPEN, request.getStatus());
        assertRequestDetailsRetained();
    }

    private void assertRequestDetailsRetained() {
        assertSame(request, service.findById("R1").orElseThrow());
        assertSame(request, service.findByTimeSlot(slot).orElseThrow());
        assertEquals(List.of(request), service.getRequests());
        assertSame(ALICE, request.getCreator());
        assertSame(slot, request.getTimeSlot());
        assertEquals(5, request.getGroupSize());
        assertEquals(List.of(ALICE, BOB), request.getParticipants());
        assertEquals(4, request.getSpotsRemaining());
    }
}
