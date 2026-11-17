package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for logging in, AccountService.logIn (ID002, TID009-TID010 and TID012). */
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
    void staffLogInStartsAnActiveSessionWithTheStaffAccountAndRole() {
        Account staff = service.createAccount("Gina Park", "gina.park@mcgill.ca", "Staff!2026");

        Session session = service.logIn("gina.park@mcgill.ca", "Staff!2026");

        assertSame(staff, session.account());
        assertEquals(new User("Gina Park"), session.user());
        assertEquals(Role.STAFF, session.role());
        assertSame(session, service.getActiveSession().orElseThrow());
        assertTrue(service.isLoggedIn(staff.getUser()));
        assertFalse(service.isLoggedIn(ALICE));
        assertFalse(service.isLoggedIn(FRANK));
    }

    @Test
    void loggingInAgainAsTheSameAccountStartsANewActiveSession() {
        Session firstSession = service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");

        Session nextSession = service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");

        assertNotSame(firstSession, nextSession);
        assertSame(alice, nextSession.account());
        assertEquals(ALICE, nextSession.user());
        assertEquals(Role.STUDENT, nextSession.role());
        assertSame(nextSession, service.getActiveSession().orElseThrow());
        assertTrue(service.isLoggedIn(ALICE));
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

    @ParameterizedTest
    @MethodSource("invalidCredentials")
    void invalidCredentialsPreserveTheActiveSessionAndRole(String email, String password) {
        Session frankSession = service.logIn("frank.ho@mcgill.ca", "Admin!2026");

        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn(email, password));

        assertEquals("Invalid email address or password", e.getMessage());
        Session activeSession = service.getActiveSession().orElseThrow();
        assertSame(frankSession, activeSession);
        assertSame(frank, activeSession.account());
        assertEquals(FRANK, activeSession.user());
        assertEquals(Role.FACILITY_ADMINISTRATOR, activeSession.role());
        assertTrue(service.isLoggedIn(FRANK));
        assertFalse(service.isLoggedIn(ALICE));
    }

    private static Stream<Arguments> invalidCredentials() {
        return Stream.of(
                Arguments.of("dan.lee@mail.mcgill.ca", "Racket!2026"),
                Arguments.of(null, "Racket!2026"),
                Arguments.of("", "Racket!2026"),
                Arguments.of("   ", "Racket!2026"),
                Arguments.of("alice.chen@mail.mcgill.ca", null),
                Arguments.of("alice.chen@mail.mcgill.ca", ""),
                Arguments.of("alice.chen@mail.mcgill.ca", "   "));
    }

    @Test
    void successfulLogInAfterARejectedAttemptReplacesThePreviousSession() {
        Session frankSession = service.logIn("frank.ho@mcgill.ca", "Admin!2026");
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.logIn("alice.chen@mail.mcgill.ca", "WrongPass1"));
        assertEquals("Invalid email address or password", e.getMessage());
        assertSame(frankSession, service.getActiveSession().orElseThrow());

        Session aliceSession = service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");

        assertNotSame(frankSession, aliceSession);
        assertSame(aliceSession, service.getActiveSession().orElseThrow());
        assertSame(alice, aliceSession.account());
        assertEquals(ALICE, aliceSession.user());
        assertEquals(Role.STUDENT, aliceSession.role());
        assertTrue(service.isLoggedIn(ALICE));
        assertFalse(service.isLoggedIn(FRANK));
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
