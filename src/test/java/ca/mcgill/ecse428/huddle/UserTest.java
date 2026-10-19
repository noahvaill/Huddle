package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void nameIsStoredAndStripped() {
        User user = new User("  Alice Chen  ");
        assertEquals("Alice Chen", user.name());
    }

    @Test
    void nullNameIsRejected() {
        NullPointerException ex =
                assertThrows(NullPointerException.class, () -> new User(null));
        assertEquals("User name is required", ex.getMessage());
    }

    @Test
    void blankNameIsRejected() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> new User("   "));
        assertEquals("User name must not be blank", ex.getMessage());
    }

    @Test
    void sameNameWithSpacesIsEqualAndSameHashCode() {
        User a = new User(" Alice Chen ");
        User b = new User("Alice Chen");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringIsTheName() {
        assertEquals("Alice Chen", new User("Alice Chen").toString());
    }
}
