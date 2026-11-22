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

    private static final String ALICE_EMAIL = "alice.chen@mail.mcgill.ca";

    private AccountService service;
    private Account alice;
    private Account frank;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        // The accounts from the ID013 feature background
        alice = new Account(new User("Alice Chen"), ALICE_EMAIL, "Racket!2026", Role.STUDENT);
        frank = new Account(new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026", Role.FACILITY_ADMINISTRATOR);
        service.addAccount(alice);
        service.addAccount(frank);
    }

    private void assertCurrentPasswordIncorrect(String currentPassword) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.changePassword(ALICE_EMAIL, currentPassword, "Shuttle!2026"));
        assertEquals(InvalidAccountException.CURRENT_PASSWORD_INCORRECT_MESSAGE, e.getMessage());
    }

    @Test
    void passwordIsChangedWhenTheCurrentOneIsCorrect() {
        Account changed = service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertSame(alice, changed);
        assertTrue(alice.passwordMatches("Shuttle!2026"));
        assertFalse(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void accountIsFoundByEmailInAnyCase() {
        service.changePassword("  Alice.Chen@MAIL.mcgill.ca ", "Racket!2026", "Shuttle!2026");

        assertTrue(alice.passwordMatches("Shuttle!2026"));
    }

    @Test
    void userCanLogInWithTheNewPasswordButNotTheOldOne() {
        service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertSame(alice, service.logIn(ALICE_EMAIL, "Shuttle!2026").account());
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn(ALICE_EMAIL, "Racket!2026"));
        assertEquals(InvalidAccountException.INVALID_CREDENTIALS_MESSAGE, e.getMessage());
    }

    @Test
    void restOfTheAccountIsKept() {
        service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertEquals("Alice Chen", alice.getName());
        assertEquals(ALICE_EMAIL, alice.getEmail());
        assertEquals(Role.STUDENT, alice.getRole());
    }

    @Test
    void activeSessionIsKept() {
        service.logIn(ALICE_EMAIL, "Racket!2026");

        service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertTrue(service.isLoggedIn(new User("Alice Chen")));
    }

    @Test
    void otherAccountsAreNotChanged() {
        service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertTrue(frank.passwordMatches("Admin!2026"));
    }

    @Test
    void secondAccountWithTheSameNameCanChangeItsOwnPassword() {
        Account first = service.createAccount("Alex Kim", "alex.kim@mail.mcgill.ca", "First!2026");
        Account second = service.createAccount("Alex Kim", "alex.kim2@mail.mcgill.ca", "Second!2026");

        service.changePassword("alex.kim2@mail.mcgill.ca", "Second!2026", "Shuttle!2026");

        assertTrue(second.passwordMatches("Shuttle!2026"));
        assertTrue(first.passwordMatches("First!2026"));
    }

    @Test
    void changingOneOfTwoSameNameAccountsWithTheSamePasswordLeavesTheOtherOne() {
        Account first = service.createAccount("Alex Kim", "alex.kim@mail.mcgill.ca", "Same!2026");
        Account second = service.createAccount("Alex Kim", "alex.kim2@mail.mcgill.ca", "Same!2026");

        service.changePassword("alex.kim2@mail.mcgill.ca", "Same!2026", "Shuttle!2026");

        assertTrue(second.passwordMatches("Shuttle!2026"));
        assertTrue(first.passwordMatches("Same!2026"));
    }

    @Test
    void wrongCurrentPasswordIsRejectedAndThePasswordIsKept() {
        assertCurrentPasswordIncorrect("WrongPass1");

        assertTrue(alice.passwordMatches("Racket!2026"));
        assertFalse(alice.passwordMatches("Shuttle!2026"));
    }

    @Test
    void currentPasswordMustMatchExactly() {
        assertCurrentPasswordIncorrect("racket!2026");
        assertCurrentPasswordIncorrect(" Racket!2026 ");
        assertCurrentPasswordIncorrect(null);

        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void emptyOrMissingNewPasswordIsRejectedAndThePasswordIsKept() {
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(ALICE_EMAIL, "Racket!2026", ""));
        assertThrows(NullPointerException.class, () -> service.changePassword(ALICE_EMAIL, "Racket!2026", null));

        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void emailWithoutAnAccountIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("dan.lee@mail.mcgill.ca", "Racket!2026", "Shuttle!2026"));
    }
}
