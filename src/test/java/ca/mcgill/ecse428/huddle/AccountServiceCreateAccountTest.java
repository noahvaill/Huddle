package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for creating an account, AccountService.createAccount (ID001, TID002-TID003). */
class AccountServiceCreateAccountTest {

    private static final Account ALICE = new Account(
            new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        // Alice from the ID001 feature's Background
        service.addAccount(ALICE);
    }

    @Test
    void studentEmailCreatesAStudentAccount() {
        Account bob = service.createAccount("Bob Tremblay", "bob.tremblay@mail.mcgill.ca", "Racket!2026");

        assertEquals(new User("Bob Tremblay"), bob.getUser());
        assertEquals("bob.tremblay@mail.mcgill.ca", bob.getEmail());
        assertEquals(Role.STUDENT, bob.getRole());
        assertTrue(bob.passwordMatches("Racket!2026"));
        assertSame(bob, service.findByEmail("bob.tremblay@mail.mcgill.ca").orElseThrow());
    }

    @Test
    void staffEmailCreatesAStaffAccount() {
        Account carla = service.createAccount("Carla Dubois", "carla.dubois@mcgill.ca", "Court#2026");

        assertEquals(Role.STAFF, carla.getRole());
        assertSame(carla, service.findByEmail("carla.dubois@mcgill.ca").orElseThrow());
    }

    @Test
    void emailIsAcceptedInAnyCaseWithSurroundingSpacesAndStoredNormalized() {
        Account bob = service.createAccount("Bob Tremblay", "  Bob.Tremblay@MAIL.McGill.ca ", "Racket!2026");

        assertEquals("bob.tremblay@mail.mcgill.ca", bob.getEmail());
        assertEquals(Role.STUDENT, bob.getRole());
    }

    @Test
    void newAccountsAreAddedAfterTheExistingOnes() {
        Account bob = service.createAccount("Bob Tremblay", "bob.tremblay@mail.mcgill.ca", "Racket!2026");
        Account carla = service.createAccount("Carla Dubois", "carla.dubois@mcgill.ca", "Court#2026");

        assertEquals(List.of(ALICE, bob, carla), service.getAccounts());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dan.lee@gmail.com",                 // from the ID001 feature
        "eva.roy@umontreal.ca",              // from the ID001 feature
        "dan.lee@notmcgill.ca",              // ends with mcgill.ca but is another domain
        "dan.lee@mail.mcgill.ca.evil.com",   // McGill domain is not at the end
        "@mail.mcgill.ca",                   // nothing before the @
        "dan@lee@mcgill.ca",                 // two @
        "dan.lee.mcgill.ca",                 // no @
    })
    void nonMcGillEmailIsRejectedAndNoAccountIsAdded(String email) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.createAccount("Dan Lee", email, "Racket!2026"));

        assertEquals(InvalidAccountException.MCGILL_EMAIL_REQUIRED_MESSAGE, e.getMessage());
        assertTrue(service.findByEmail(email).isEmpty());
        assertEquals(List.of(ALICE), service.getAccounts());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void missingEmailIsRejectedAsNotMcGill(String email) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.createAccount("Dan Lee", email, "Racket!2026"));

        assertEquals(InvalidAccountException.MCGILL_EMAIL_REQUIRED_MESSAGE, e.getMessage());
        assertEquals(List.of(ALICE), service.getAccounts());
    }

    @Test
    void emailAlreadyInUseIsRejectedAndTheExistingAccountIsKept() {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.createAccount("Alice Chen", "alice.chen@mail.mcgill.ca", "Racket!2026"));

        assertEquals(InvalidAccountException.EMAIL_ALREADY_IN_USE_MESSAGE, e.getMessage());
        assertEquals(List.of(ALICE), service.getAccounts());
    }

    @Test
    void emailAlreadyInUseIsRejectedInAnyCase() {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.createAccount("Alice C", " ALICE.CHEN@mail.mcgill.ca", "Other!2026"));

        assertEquals(InvalidAccountException.EMAIL_ALREADY_IN_USE_MESSAGE, e.getMessage());
        assertSame(ALICE, service.findByEmail("alice.chen@mail.mcgill.ca").orElseThrow());
    }

    @Test
    void nonMcGillEmailIsReportedBeforeEmailAlreadyInUse() {
        service.addAccount(new Account(new User("Dan Lee"), "dan.lee@gmail.com", "Racket!2026", Role.STUDENT));

        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> service.createAccount("Dan Lee", "dan.lee@gmail.com", "Racket!2026"));

        assertEquals(InvalidAccountException.MCGILL_EMAIL_REQUIRED_MESSAGE, e.getMessage());
    }

    @Test
    void emptyPasswordOrBlankNameIsRejectedAndNoAccountIsAdded() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createAccount("Bob Tremblay", "bob.tremblay@mail.mcgill.ca", ""));
        assertThrows(IllegalArgumentException.class,
                () -> service.createAccount("   ", "bob.tremblay@mail.mcgill.ca", "Racket!2026"));

        assertTrue(service.findByEmail("bob.tremblay@mail.mcgill.ca").isEmpty());
    }

    @Test
    void confirmationMessageMatchesTheFeatureFile() {
        assertEquals("Account created", AccountService.ACCOUNT_CREATED_MESSAGE);
    }
}
