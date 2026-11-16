package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AccountTest {

    private static final User ALICE = new User("Alice Chen");

    private static Account alice() {
        return new Account(ALICE, "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
    }

    @Test
    void storesUserEmailAndRole() {
        Account a = alice();
        assertEquals(ALICE, a.getUser());
        assertEquals("Alice Chen", a.getName());
        assertEquals("alice.chen@mail.mcgill.ca", a.getEmail());
        assertEquals(Role.STUDENT, a.getRole());
    }

    @Test
    void emailIsStrippedAndLowerCased() {
        Account a = new Account(ALICE, "  Alice.Chen@Mail.McGill.ca ", "Racket!2026", Role.STUDENT);
        assertEquals("alice.chen@mail.mcgill.ca", a.getEmail());
    }

    @Test
    void passwordMatchesOnlyTheExactPassword() {
        Account a = alice();
        assertTrue(a.passwordMatches("Racket!2026"));
        assertFalse(a.passwordMatches("racket!2026"));   // case-sensitive
        assertFalse(a.passwordMatches(" Racket!2026"));  // not stripped
        assertFalse(a.passwordMatches("WrongPass1"));
        assertFalse(a.passwordMatches(null));
    }

    @Test
    void missingOrBlankFieldsAreRejected() {
        assertEquals("User is required", assertThrows(NullPointerException.class,
                () -> new Account(null, "a@mail.mcgill.ca", "pw", Role.STUDENT)).getMessage());
        assertEquals("Email is required", assertThrows(NullPointerException.class,
                () -> new Account(ALICE, null, "pw", Role.STUDENT)).getMessage());
        assertEquals("Email must not be blank", assertThrows(IllegalArgumentException.class,
                () -> new Account(ALICE, "   ", "pw", Role.STUDENT)).getMessage());
        assertEquals("Password is required", assertThrows(NullPointerException.class,
                () -> new Account(ALICE, "a@mail.mcgill.ca", null, Role.STUDENT)).getMessage());
        assertEquals("Password must not be empty", assertThrows(IllegalArgumentException.class,
                () -> new Account(ALICE, "a@mail.mcgill.ca", "", Role.STUDENT)).getMessage());
        assertEquals("Role is required", assertThrows(NullPointerException.class,
                () -> new Account(ALICE, "a@mail.mcgill.ca", "pw", null)).getMessage());
    }

    @Test
    void toStringNeverShowsThePassword() {
        String s = alice().toString();
        assertEquals("Alice Chen <alice.chen@mail.mcgill.ca> (Student)", s);
        assertFalse(s.contains("Racket!2026"));
    }
}
