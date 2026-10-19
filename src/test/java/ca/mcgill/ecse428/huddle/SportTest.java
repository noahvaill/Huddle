package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SportTest {

    @Test
    void nameIsStored() {
        assertEquals("Squash", new Sport("Squash").name());
    }

    @Test
    void nameIsTrimmed() {
        assertEquals(new Sport("Squash"), new Sport("  Squash "));
    }

    @Test
    void blankNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Sport("  "));
    }

    @Test
    void nullNameIsRejected() {
        assertThrows(NullPointerException.class, () -> new Sport(null));
    }
}
