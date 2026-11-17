package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for logging in, AccountService.logIn (ID002, TID009-TID010). */
class AccountServiceLogInTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User FRANK = new User("Frank Ho");

    private AccountService service;
    private Account alice;
    private Account frank;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        // Alice from the ID002 feature's Background, and Frank from ID003's
        alice = new Account(ALICE, "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
        frank = new Account(FRANK, "frank.ho@mcgill.ca", "Admin!2026", Role.FACILITY_ADMINISTRATOR);
        service.addAccount(alice);
        service.addAccount(frank);
    }

    @Test
    void correctCredentialsStartAnActiveSessionWithTheUsersRole() {
        Session session = service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");

        assertSame(alice, session.account());
        assertEquals(ALICE, session.user());
        assertEquals(Role.STUDENT, session.role());
        assertSame(session, service.getActiveSession().orElseThrow());
        assertTrue(service.isLoggedIn(ALICE));
    }

    @Test
    void sessionCarriesTheFacilityAdministratorRole() {
        Session session = service.logIn("frank.ho@mcgill.ca", "Admin!2026");

        assertEquals(Role.FACILITY_ADMINISTRATOR, session.role());
    }

    @Test
    void emailIsMatchedInAnyCaseAndWithSurroundingSpaces() {
        Session session = service.logIn("  Alice.Chen@MAIL.MCGILL.CA ", "Racket!2026");

        assertSame(alice, session.account());
        assertTrue(service.isLoggedIn(ALICE));
    }

    @Test
    void loggedInMessageMatchesTheFeatureFile() {
        assertEquals("Welcome back, Alice Chen", AccountService.loggedInMessage(alice));
    }

    @Test
    void loggingInAsAnotherUserReplacesTheActiveSession() {
        service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");
        service.logIn("frank.ho@mcgill.ca", "Admin!2026");

        assertTrue(service.isLoggedIn(FRANK));
        assertFalse(service.isLoggedIn(ALICE));
    }

    @Test
    void nobodyIsLoggedInBeforeTheFirstLogIn() {
        assertTrue(service.getActiveSession().isEmpty());
        assertFalse(service.isLoggedIn(ALICE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WrongPass1", "racket!2026", " Racket!2026", "Admin!2026"})
    void wrongPasswordIsRejectedAndNobodyIsLoggedIn(String password) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn("alice.chen@mail.mcgill.ca", password));

        assertEquals("Invalid email address or password", e.getMessage());
        assertFalse(service.isLoggedIn(ALICE));
        assertTrue(service.getActiveSession().isEmpty());
    }

    @Test
    void unknownEmailGivesTheSameErrorAsAWrongPassword() {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn("dan.lee@mail.mcgill.ca", "Racket!2026"));

        assertEquals(InvalidAccountException.INVALID_CREDENTIALS_MESSAGE, e.getMessage());
        assertTrue(service.getActiveSession().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void missingEmailIsRejected(String email) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn(email, "Racket!2026"));

        assertEquals(InvalidAccountException.INVALID_CREDENTIALS_MESSAGE, e.getMessage());
    }

    @Test
    void missingPasswordIsRejected() {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn("alice.chen@mail.mcgill.ca", null));

        assertEquals(InvalidAccountException.INVALID_CREDENTIALS_MESSAGE, e.getMessage());
    }

    @Test
    void failedLogInKeepsTheSessionThatWasActive() {
        Session frankSession = service.logIn("frank.ho@mcgill.ca", "Admin!2026");

        assertThrows(InvalidAccountException.class, () -> service.logIn("alice.chen@mail.mcgill.ca", "WrongPass1"));

        assertSame(frankSession, service.getActiveSession().orElseThrow());
        assertFalse(service.isLoggedIn(ALICE));
    }

    @Test
    void accountCreatedWithCreateAccountCanLogIn() {
        service.createAccount("Bob Tremblay", "bob.tremblay@mail.mcgill.ca", "Racket!2026");

        Session session = service.logIn("bob.tremblay@mail.mcgill.ca", "Racket!2026");

        assertEquals(new User("Bob Tremblay"), session.user());
        assertEquals(Role.STUDENT, session.role());
    }

    @Test
    void sessionRequiresAnAccount() {
        assertThrows(NullPointerException.class, () -> new Session(null));
    }
}
