package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameRequestTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User BOB = new User("Bob Martin");
    private static final User DAN = new User("Dan Lee");
    private static final User CARLA = new User("Carla Diaz");

    private static final TimeSlot SQUASH = new TimeSlot(new Court("Squash Court 1", Sport.SQUASH),
            LocalDate.of(2026, 10, 15), LocalTime.of(18, 0), LocalTime.of(18, 45), true);
    private static final TimeSlot BASKETBALL = new TimeSlot(new Court("Basketball Court", Sport.BASKETBALL),
            LocalDate.of(2026, 10, 15), LocalTime.of(19, 0), LocalTime.of(20, 0), true);

    // --- constructors and participant counting ---

    @Test
    void fiveArgConstructorIsOpenWithCreatorOnly() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);

        assertEquals(GameRequestStatus.OPEN, r.getStatus());
        assertEquals(List.of(ALICE), r.getParticipants());
        assertEquals(1, r.getParticipantCount());
    }

    @Test
    void basketballGroupCountsAllPlayers() {
        GameRequest r = new GameRequest("R1", ALICE, BASKETBALL, 5, 5);
        assertEquals(5, r.getParticipantCount());
    }

    @Test
    void sevenArgId007R5CountsTwo() {
        GameRequest r = new GameRequest("R5", DAN, SQUASH, 1, 2,
                GameRequestStatus.OPEN, List.of(DAN, BOB));

        assertEquals(2, r.getParticipantCount());
        assertTrue(r.hasParticipant(BOB));
    }

    @Test
    void sevenArgId010R2CountsNine() {
        GameRequest r = new GameRequest("R2", CARLA, SQUASH, 9, 1,
                GameRequestStatus.OPEN, List.of(CARLA));
        assertEquals(9, r.getParticipantCount());
    }

    @Test
    void confirmedWithNoSpotsIsNotOpenAndFull() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 0,
                GameRequestStatus.CONFIRMED, List.of(ALICE));

        assertFalse(r.isOpen());
        assertTrue(r.isFull());
    }

    @Test
    void getSportIsTheSlotsSport() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        assertEquals(Sport.SQUASH, r.getSport());
    }

    // --- query helpers ---

    @Test
    void isCreatedBy() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        assertTrue(r.isCreatedBy(ALICE));
        assertFalse(r.isCreatedBy(BOB));
        assertFalse(r.isCreatedBy(null));
    }

    @Test
    void hasParticipant() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        assertFalse(r.hasParticipant(BOB));
        assertFalse(r.hasParticipant(null));
    }

    @Test
    void getParticipantsIsUnmodifiable() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        assertThrows(UnsupportedOperationException.class, () -> r.getParticipants().add(BOB));
    }

    @Test
    void mutatingCallersListDoesNotAffectRequest() {
        List<User> participants = new ArrayList<>(List.of(ALICE));
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1,
                GameRequestStatus.OPEN, participants);

        participants.add(BOB);

        assertEquals(List.of(ALICE), r.getParticipants());
    }

    // --- mutators ---

    @Test
    void addParticipantAddsUser() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        r.addParticipant(BOB);

        assertTrue(r.hasParticipant(BOB));
        assertEquals(List.of(ALICE, BOB), r.getParticipants());
    }

    @Test
    void addParticipantRejectsNull() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        NullPointerException ex = assertThrows(NullPointerException.class, () -> r.addParticipant(null));
        assertEquals("Participant is required", ex.getMessage());
    }

    @Test
    void addParticipantRejectsDuplicate() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> r.addParticipant(ALICE));
        assertEquals("A user cannot participate twice", ex.getMessage());
    }

    @Test
    void setSpotsRemainingRejectsNegative() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> r.setSpotsRemaining(-1));
        assertEquals("Spots remaining must not be negative", ex.getMessage());
    }

    @Test
    void setStatusRejectsNull() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        NullPointerException ex = assertThrows(NullPointerException.class, () -> r.setStatus(null));
        assertEquals("Status is required", ex.getMessage());
    }

    // --- constructor validation (one per row) ---

    @Test
    void nullIdIsRejected() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new GameRequest(null, ALICE, SQUASH, 1, 1));
        assertEquals("Game request id is required", ex.getMessage());
    }

    @Test
    void blankIdIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new GameRequest("   ", ALICE, SQUASH, 1, 1));
        assertEquals("Game request id must not be blank", ex.getMessage());
    }

    @Test
    void nullCreatorIsRejected() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new GameRequest("R1", null, SQUASH, 1, 1));
        assertEquals("Creator is required", ex.getMessage());
    }

    @Test
    void nullTimeSlotIsRejected() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new GameRequest("R1", ALICE, null, 1, 1));
        assertEquals("Time slot is required", ex.getMessage());
    }

    @Test
    void nullStatusIsRejected() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 1, 1, null, List.of(ALICE)));
        assertEquals("Status is required", ex.getMessage());
    }

    @Test
    void nullParticipantsIsRejected() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 1, 1, GameRequestStatus.OPEN, null));
        assertEquals("Participants are required", ex.getMessage());
    }

    @Test
    void groupSizeBelowOneIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 0, 1));
        assertEquals("Group size must be at least 1", ex.getMessage());
    }

    @Test
    void negativeSpotsRemainingIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 1, -1));
        assertEquals("Spots remaining must not be negative", ex.getMessage());
    }

    @Test
    void emptyParticipantsIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 1, 1, GameRequestStatus.OPEN, List.of()));
        assertEquals("Participants must not be empty", ex.getMessage());
    }

    @Test
    void participantsContainingNullIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 2, 0, GameRequestStatus.OPEN,
                        Arrays.asList(ALICE, null)));
        assertEquals("Participants must not contain null", ex.getMessage());
    }

    @Test
    void firstParticipantNotCreatorIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 1, 1, GameRequestStatus.OPEN, List.of(BOB)));
        assertEquals("The creator must be the first participant", ex.getMessage());
    }

    @Test
    void duplicateParticipantIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new GameRequest("R1", ALICE, SQUASH, 2, 0, GameRequestStatus.OPEN, List.of(ALICE, ALICE)));
        assertEquals("A user cannot participate twice", ex.getMessage());
    }

    // --- equals / hashCode / toString ---

    @Test
    void equalsAndHashCodeOnIdOnly() {
        GameRequest a = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        GameRequest b = new GameRequest("R1", BOB, BASKETBALL, 2, 2);
        GameRequest c = new GameRequest("R2", ALICE, SQUASH, 1, 1);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toStringMatchesFormat() {
        GameRequest r = new GameRequest("R1", ALICE, SQUASH, 1, 1);
        assertEquals("R1 by Alice Chen, Squash 2026-10-15 18:00, 1 spot(s) left, Open", r.toString());
    }
}
