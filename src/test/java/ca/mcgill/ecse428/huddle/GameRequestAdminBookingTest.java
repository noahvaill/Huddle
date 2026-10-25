package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID010: a facility admin books the time slot while a user is joining the game (TID034 test hook). */
class GameRequestAdminBookingTest {

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
    void adminBookingTheSlotDuringJoinForLastSpotCancelsGameAndNotifiesCreator() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        service.addRequest(new GameRequest("R1", ALICE, s1, 1, 1, GameRequestStatus.OPEN, List.of(ALICE)));
        service.setBeforeSlotCheckHook(request -> request.getTimeSlot().book());

        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.joinRequest(BOB, "R1"));

        GameRequest r1 = service.findById("R1").orElseThrow();
        assertEquals(InvalidGameRequestException.SLOT_NO_LONGER_AVAILABLE_MESSAGE, e.getMessage());
        assertEquals(GameRequestStatus.CANCELLED, r1.getStatus());
        assertFalse(r1.hasParticipant(BOB));
        assertEquals(1, r1.getSpotsRemaining());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertEquals(List.of("The Squash game on 2026-10-15 at 18:00 has been cancelled: time slot no longer available"),
                service.getNotificationsFor(ALICE));
        assertTrue(service.getNotificationsFor(BOB).isEmpty());
    }

    @Test
    void adminBookingTheSlotDuringJoinOfGroupGameCancelsIt() {
        TimeSlot s3 = slot("Basketball Court", Sport.BASKETBALL, "19:00", "20:00");
        service.addRequest(new GameRequest("R2", CARLA, s3, 5, 5, GameRequestStatus.OPEN, List.of(CARLA)));
        service.setBeforeSlotCheckHook(request -> request.getTimeSlot().book());

        InvalidGameRequestException e = assertThrows(InvalidGameRequestException.class,
                () -> service.joinRequest(BOB, "R2"));

        GameRequest r2 = service.findById("R2").orElseThrow();
        assertEquals(InvalidGameRequestException.SLOT_NO_LONGER_AVAILABLE_MESSAGE, e.getMessage());
        assertEquals(GameRequestStatus.CANCELLED, r2.getStatus());
        assertFalse(r2.hasParticipant(BOB));
        assertEquals(5, r2.getSpotsRemaining());
        assertEquals(1, service.getNotificationsFor(CARLA).size());
    }

    @Test
    void hookIsGivenTheRequestBeingJoined() {
        TimeSlot s3 = slot("Basketball Court", Sport.BASKETBALL, "19:00", "20:00");
        service.addRequest(new GameRequest("R2", CARLA, s3, 5, 5, GameRequestStatus.OPEN, List.of(CARLA)));
        List<String> seen = new ArrayList<>();
        service.setBeforeSlotCheckHook(request -> seen.add(request.getId()));

        service.joinRequest(BOB, "R2");

        assertEquals(List.of("R2"), seen);
    }

    @Test
    void withoutHookJoinForLastSpotStillConfirmsAndBooks() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        service.addRequest(new GameRequest("R1", ALICE, s1, 1, 1, GameRequestStatus.OPEN, List.of(ALICE)));

        GameRequest r1 = service.joinRequest(BOB, "R1");

        assertEquals(GameRequestStatus.CONFIRMED, r1.getStatus());
        assertEquals(TimeSlotStatus.BOOKED, s1.status());
        assertTrue(service.getNotificationsFor(ALICE).isEmpty());
    }

    @Test
    void resettingHookToNullRestoresNormalJoin() {
        TimeSlot s1 = slot("Squash Court 1", Sport.SQUASH, "18:00", "18:45");
        service.addRequest(new GameRequest("R1", ALICE, s1, 1, 1, GameRequestStatus.OPEN, List.of(ALICE)));
        service.setBeforeSlotCheckHook(request -> request.getTimeSlot().book());
        service.setBeforeSlotCheckHook(null);

        GameRequest r1 = service.joinRequest(BOB, "R1");

        assertEquals(GameRequestStatus.CONFIRMED, r1.getStatus());
    }
}
