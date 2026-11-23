package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the edit profile rules of ID013 as a whole (TID045): the feature file's inputs, a failed change
 * leaving the whole account as it was, and name and password changes working together.
 * The single-rule tests are in AccountServiceChangeNameTest (TID041, TID043) and AccountServiceChangePasswordTest
 * (TID042).
 */
class AccountServiceEditProfileTest {

    private static final String ALICE_EMAIL = "alice.chen@mail.mcgill.ca";
    private static final String FRANK_EMAIL = "frank.ho@mcgill.ca";

    private AccountService service;
    private Account alice;
    private Account frank;

    @BeforeEach
    void setUp() {
        // The Background of the ID013 feature: both accounts, and Alice logged in
        service = new AccountService();
        alice = new Account(new User("Alice Chen"), ALICE_EMAIL, "Racket!2026", Role.STUDENT);
        frank = new Account(new User("Frank Ho"), FRANK_EMAIL, "Admin!2026", Role.FACILITY_ADMINISTRATOR);
        service.addAccount(alice);
        service.addAccount(frank);
        service.logIn(ALICE_EMAIL, "Racket!2026");
    }

    private InvalidAccountException assertRejected(Runnable change, String expectedMessage) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class, change::run);
        assertEquals(expectedMessage, e.getMessage());
        return e;
    }

    private void assertAliceUnchanged() {
        assertEquals("Alice Chen", alice.getName());
        assertEquals(ALICE_EMAIL, alice.getEmail());
        assertEquals(Role.STUDENT, alice.getRole());
        assertTrue(alice.passwordMatches("Racket!2026"));
        assertTrue(service.isLoggedIn(new User("Alice Chen")));
    }

    @Test
    void errorMessagesAreTheOnesTheFeatureFileExpects() {
        assertEquals("Current password is incorrect", InvalidAccountException.CURRENT_PASSWORD_INCORRECT_MESSAGE);
        assertEquals("Name cannot be empty", InvalidAccountException.NAME_EMPTY_MESSAGE);
    }

    @Test
    void emptyNameFromTheFeatureFileLeavesTheWholeAccountUnchanged() {
        assertRejected(() -> service.changeName(new User("Alice Chen"), ""), InvalidAccountException.NAME_EMPTY_MESSAGE);

        assertAliceUnchanged();
    }

    @Test
    void incorrectCurrentPasswordLeavesTheWholeAccountUnchanged() {
        assertRejected(() -> service.changePassword(ALICE_EMAIL, "WrongPass1", "Shuttle!2026"),
                InvalidAccountException.CURRENT_PASSWORD_INCORRECT_MESSAGE);

        assertAliceUnchanged();
        assertEquals(alice, service.logIn(ALICE_EMAIL, "Racket!2026").account());
    }

    @Test
    void anotherAccountsPasswordIsNotAcceptedAsTheCurrentPassword() {
        assertRejected(() -> service.changePassword(ALICE_EMAIL, "Admin!2026", "Shuttle!2026"),
                InvalidAccountException.CURRENT_PASSWORD_INCORRECT_MESSAGE);

        assertAliceUnchanged();
        assertTrue(frank.passwordMatches("Admin!2026"));
    }

    @Test
    void correctCurrentPasswordIsAcceptedAfterAnIncorrectOne() {
        assertRejected(() -> service.changePassword(ALICE_EMAIL, "WrongPass1", "Shuttle!2026"),
                InvalidAccountException.CURRENT_PASSWORD_INCORRECT_MESSAGE);

        service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertTrue(alice.passwordMatches("Shuttle!2026"));
    }

    @Test
    void nameAndPasswordCanBothBeChanged() {
        service.changeName(new User("Alice Chen"), "Alice Chen-Roy");
        // Found by the new name, as a logged-in user changing her password would be
        String email = service.findByUser(new User("Alice Chen-Roy")).orElseThrow().getEmail();
        service.changePassword(email, "Racket!2026", "Shuttle!2026");

        Session session = service.logIn(ALICE_EMAIL, "Shuttle!2026");
        assertEquals("Welcome back, Alice Chen-Roy", AccountService.loggedInMessage(session.account()));
        assertThrows(InvalidAccountException.class, () -> service.logIn(ALICE_EMAIL, "Racket!2026"));
        assertEquals(Role.STUDENT, session.role());
    }

    @Test
    void failedNameChangeKeepsAnEarlierPasswordChange() {
        service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertRejected(() -> service.changeName(new User("Alice Chen"), ""), InvalidAccountException.NAME_EMPTY_MESSAGE);

        assertTrue(alice.passwordMatches("Shuttle!2026"));
        assertEquals("Alice Chen", alice.getName());
    }

    @Test
    void editingOneProfileLeavesTheOtherAccountsUnchanged() {
        service.changeName(new User("Alice Chen"), "Alice Chen-Roy");
        service.changePassword(ALICE_EMAIL, "Racket!2026", "Shuttle!2026");

        assertEquals("Frank Ho", frank.getName());
        assertEquals(Role.FACILITY_ADMINISTRATOR, frank.getRole());
        assertTrue(frank.passwordMatches("Admin!2026"));
    }

    @Test
    void facilityAdministratorCanEditTheirProfileAndKeepsTheirRole() {
        service.changeName(new User("Frank Ho"), "Frank Ho-Lam");
        service.changePassword(FRANK_EMAIL, "Admin!2026", "Court!2026");

        Session session = service.logIn(FRANK_EMAIL, "Court!2026");
        assertEquals(new User("Frank Ho-Lam"), session.user());
        assertEquals(Role.FACILITY_ADMINISTRATOR, session.role());
    }

    @Test
    void newPasswordIsKeptExactlyAsTyped() {
        service.changePassword(ALICE_EMAIL, "Racket!2026", " Shuttle!2026 ");

        assertTrue(alice.passwordMatches(" Shuttle!2026 "));
        assertFalse(alice.passwordMatches("Shuttle!2026"));
        assertFalse(alice.passwordMatches(" shuttle!2026 "));
    }
}