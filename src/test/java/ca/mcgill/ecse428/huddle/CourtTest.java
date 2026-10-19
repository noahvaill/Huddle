package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CourtTest {

    private final Sport squash = new Sport("Squash");

    @Test
    void nameAndSportAreStored() {
        Court court = new Court("Squash Court 1", squash);
        assertEquals("Squash Court 1", court.name());
        assertEquals(squash, court.sport());
    }

    @Test
    void blankNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Court(" ", squash));
    }

    @Test
    void missingSportIsRejected() {
        assertThrows(NullPointerException.class, () -> new Court("Squash Court 1", null));
    }
}
