package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID013: changing the password of an account after checking the current one (TID042). */
class AccountServiceChangePasswordTest {

    private static final User ALICE = new User("Alice Chen");

    private AccountService service;
    private Account alice;
    private Account frank;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        // The accounts from the ID013 feature background
        alice = new Account(ALICE, "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
        frank = new Account(new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026", Role.FACILITY_ADMINISTRATOR);
        service.addAccount(alice);
        service.addAccount(frank);
    }

    @Test
    void passwordIsChangedWhenTheCurrentOneIsCorrect() {
        Account changed = service.changePassword(ALICE, "Racket!2026", "Shuttle!2026");

        assertSame(alice, changed);
        assertTrue(alice.passwordMatches("Shuttle!2026"));
        assertFalse(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void userCanLogInWithTheNewPasswordButNotTheOldOne() {
        service.changePassword(ALICE, "Racket!2026", "Shuttle!2026");

        assertSame(alice, service.logIn("alice.chen@mail.mcgill.ca", "Shuttle!2026").account());
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026"));
        assertEquals(InvalidAccountException.INVALID_CREDENTIALS_MESSAGE, e.getMessage());
    }

    @Test
    void restOfTheAccountIsKept() {
        service.changePassword(ALICE, "Racket!2026", "Shuttle!2026");

        assertEquals("Alice Chen", alice.getName());
        assertEquals("alice.chen@mail.mcgill.ca", alice.getEmail());
        assertEquals(Role.STUDENT, alice.getRole());
    }

    @Test
    void activeSessionIsKept() {
        service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");

        service.changePassword(ALICE, "Racket!2026", "Shuttle!2026");

        assertTrue(service.isLoggedIn(ALICE));
    }

    @Test
    void otherAccountsAreNotChanged() {
        service.changePassword(ALICE, "Racket!2026", "Shuttle!2026");

        assertTrue(frank.passwordMatches("Admin!2026"));
    }

    @Test
    void wrongCurrentPasswordIsRejectedAndThePasswordIsKept() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(ALICE, "WrongPass1", "Shuttle!2026"));

        assertTrue(alice.passwordMatches("Racket!2026"));
        assertFalse(alice.passwordMatches("Shuttle!2026"));
    }

    @Test
    void currentPasswordMustMatchExactly() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(ALICE, "racket!2026", "Shuttle!2026"));
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(ALICE, " Racket!2026 ", "Shuttle!2026"));
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(ALICE, null, "Shuttle!2026"));

        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void emptyOrMissingNewPasswordIsRejectedAndThePasswordIsKept() {
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(ALICE, "Racket!2026", ""));
        assertThrows(NullPointerException.class, () -> service.changePassword(ALICE, "Racket!2026", null));

        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void userWithoutAnAccountIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(new User("Dan Lee"), "Racket!2026", "Shuttle!2026"));
    }
}
