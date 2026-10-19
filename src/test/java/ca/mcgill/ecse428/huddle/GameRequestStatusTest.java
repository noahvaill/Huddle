package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GameRequestStatusTest {

    @Test
    void displayNames() {
        assertEquals("Open", GameRequestStatus.OPEN.getDisplayName());
        assertEquals("Confirmed", GameRequestStatus.CONFIRMED.getDisplayName());
        assertEquals("Cancelled", GameRequestStatus.CANCELLED.getDisplayName());
    }

    @Test
    void fromNameMatchesIgnoringCaseAndSpaces() {
        assertEquals(GameRequestStatus.OPEN, GameRequestStatus.fromName("open"));
        assertEquals(GameRequestStatus.CONFIRMED, GameRequestStatus.fromName(" CONFIRMED "));
        assertEquals(GameRequestStatus.CANCELLED, GameRequestStatus.fromName("Cancelled"));
    }

    @Test
    void unknownNameIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GameRequestStatus.fromName("Pending"));
        assertEquals("Unknown game request status: Pending", ex.getMessage());
    }

    @Test
    void nullNameIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GameRequestStatus.fromName(null));
        assertEquals("Unknown game request status: null", ex.getMessage());
    }
}
