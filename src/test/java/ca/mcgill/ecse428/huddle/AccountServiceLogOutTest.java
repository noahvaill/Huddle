package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for logging out, AccountService.logOut (ID012, TID033-TID034). */
class AccountServiceLogOutTest {

    private static final User ALICE = new User("Alice Chen");
    private static final User FRANK = new User("Frank Ho");

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        // The accounts in the ID012 feature's Background
        service.addAccount(new Account(ALICE, "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT));
        service.addAccount(new Account(FRANK, "frank.ho@mcgill.ca", "Admin!2026", Role.FACILITY_ADMINISTRATOR));
    }

    private Session logInAlice(String device) {
        return service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026", device);
    }

    private Session logInFrank(String device) {
        return service.logIn("frank.ho@mcgill.ca", "Admin!2026", device);
    }

    @Test
    void sameAccountCanBeLoggedInOnTwoDevices() {
        Session laptop = logInAlice("Laptop");
        Session phone = logInAlice("Phone");

        assertSame(laptop, service.getActiveSession("Laptop").orElseThrow());
        assertSame(phone, service.getActiveSession("Phone").orElseThrow());
    }

    @Test
    void loggingOutOnOneDeviceKeepsTheSessionOnTheOther() {
        logInAlice("Laptop");
        Session phone = logInAlice("Phone");

        service.logOut("Laptop");

        assertFalse(service.isLoggedIn(ALICE, "Laptop"));
        assertTrue(service.isLoggedIn(ALICE, "Phone"));
        assertTrue(service.isLoggedIn(ALICE));
        assertSame(phone, service.getActiveSession("Phone").orElseThrow());
    }

    @Test
    void loggingOutOnTheLatestDeviceMakesTheEarlierSessionTheActiveOne() {
        Session laptop = logInAlice("Laptop");
        logInAlice("Phone");

        service.logOut("Phone");

        assertSame(laptop, service.getActiveSession().orElseThrow());
    }

    @Test
    void loggingOutOnEveryDeviceLeavesNoActiveSession() {
        logInAlice("Laptop");
        logInAlice("Phone");

        service.logOut("Laptop");
        service.logOut("Phone");

        assertFalse(service.isLoggedIn(ALICE));
        assertTrue(service.getActiveSession().isEmpty());
    }

    @Test
    void loggingOutKeepsOtherUsersSessions() {
        logInAlice("Laptop");
        logInFrank("Desktop");

        service.logOut("Laptop");

        assertFalse(service.isLoggedIn(ALICE));
        assertTrue(service.isLoggedIn(FRANK, "Desktop"));
    }

    @Test
    void loggingInOnADeviceReplacesOnlyThatDevicesSession() {
        logInAlice("Laptop");
        logInAlice("Phone");

        logInFrank("Laptop");

        assertTrue(service.isLoggedIn(FRANK, "Laptop"));
        assertFalse(service.isLoggedIn(ALICE, "Laptop"));
        assertTrue(service.isLoggedIn(ALICE, "Phone"));
    }

    @Test
    void activeSessionIsTheLatestLogInEvenOnADeviceUsedBefore() {
        logInAlice("Laptop");
        logInAlice("Phone");

        Session laptopAgain = logInAlice("Laptop");

        assertSame(laptopAgain, service.getActiveSession().orElseThrow());
    }

    @Test
    void logInWithoutADeviceUsesTheDefaultDevice() {
        service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");
        assertTrue(service.isLoggedIn(ALICE, AccountService.DEFAULT_DEVICE));

        service.logOut(AccountService.DEFAULT_DEVICE);

        assertTrue(service.getActiveSession().isEmpty());
    }

    @Test
    void userCanLogInAgainAfterLoggingOut() {
        logInAlice("Laptop");
        service.logOut("Laptop");

        logInAlice("Laptop");

        assertTrue(service.isLoggedIn(ALICE, "Laptop"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankDeviceIsRejected(String device) {
        assertThrows(IllegalArgumentException.class, () -> logInAlice(device));
        assertThrows(IllegalArgumentException.class, () -> service.logOut(device));
        assertTrue(service.getActiveSession().isEmpty());
    }

    @Test
    void missingDeviceIsRejected() {
        assertThrows(NullPointerException.class, () -> logInAlice(null));
        assertThrows(NullPointerException.class, () -> service.logOut(null));
    }

    private void assertLogOutIsRejected(String device) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class, () -> service.logOut(device));
        assertEquals("You are not logged in", e.getMessage());
    }

    @Test
    void loggingOutWithoutAnActiveSessionIsRejected() {
        assertLogOutIsRejected(AccountService.DEFAULT_DEVICE);

        assertTrue(service.getActiveSession().isEmpty());
    }

    @Test
    void loggingOutTwiceIsRejectedTheSecondTime() {
        logInAlice("Laptop");
        service.logOut("Laptop");

        assertLogOutIsRejected("Laptop");
    }

    @Test
    void loggingOutOnADeviceWithoutASessionIsRejectedAndKeepsTheOtherDevicesSession() {
        Session phone = logInAlice("Phone");

        assertLogOutIsRejected("Laptop");

        assertSame(phone, service.getActiveSession("Phone").orElseThrow());
        assertTrue(service.isLoggedIn(ALICE));
    }

    @Test
    void loggingOutAfterAFailedLogInIsRejected() {
        assertThrows(InvalidAccountException.class,
                () -> service.logIn("alice.chen@mail.mcgill.ca", "WrongPass1", "Laptop"));

        assertLogOutIsRejected("Laptop");
    }

    @Test
    void loggedOutMessageMatchesTheFeatureFile() {
        assertEquals("You have been logged out", AccountService.LOGGED_OUT_MESSAGE);
    }

    // More log out rules (TID036)

    @ParameterizedTest
    @EnumSource(Role.class)
    void accountsWithAnyRoleCanLogOut(Role role) {
        User user = new User("Gina Park");
        service.addAccount(new Account(user, "gina.park@mcgill.ca", "Staff!2026", role));
        service.logIn("gina.park@mcgill.ca", "Staff!2026");

        service.logOut(AccountService.DEFAULT_DEVICE);

        assertFalse(service.isLoggedIn(user));
        assertTrue(service.getActiveSession().isEmpty());
    }

    @Test
    void loggingOutKeepsTheAccount() {
        Account alice = logInAlice("Laptop").account();

        service.logOut("Laptop");

        assertSame(alice, service.findByEmail("alice.chen@mail.mcgill.ca").orElseThrow());
        assertEquals(2, service.getAccounts().size());
        assertEquals(Role.STUDENT, alice.getRole());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void anotherUserCanLogInOnTheDeviceAfterALogOut() {
        logInAlice("Laptop");
        service.logOut("Laptop");

        Session frank = logInFrank("Laptop");

        assertSame(frank, service.getActiveSession("Laptop").orElseThrow());
        assertEquals(Role.FACILITY_ADMINISTRATOR, frank.role());
        assertFalse(service.isLoggedIn(ALICE));
    }

    @Test
    void rejectedLogOutDoesNotPreventALaterLogInAndLogOut() {
        assertLogOutIsRejected("Laptop");

        logInAlice("Laptop");
        assertTrue(service.isLoggedIn(ALICE, "Laptop"));
        service.logOut("Laptop");

        assertFalse(service.isLoggedIn(ALICE, "Laptop"));
    }

    @Test
    void loggingInAgainOnADeviceNeedsOnlyOneLogOut() {
        logInAlice("Laptop");
        logInAlice("Laptop");

        service.logOut("Laptop");

        assertFalse(service.isLoggedIn(ALICE));
        assertLogOutIsRejected("Laptop");
    }

    @Test
    void failedLogInAfterALogOutDoesNotRestoreTheSession() {
        logInAlice("Laptop");
        service.logOut("Laptop");

        assertThrows(InvalidAccountException.class,
                () -> service.logIn("alice.chen@mail.mcgill.ca", "WrongPass1", "Laptop"));

        assertFalse(service.isLoggedIn(ALICE));
        assertTrue(service.getActiveSession("Laptop").isEmpty());
    }

    @Test
    void userCanLogOutAfterChangingPassword() {
        logInAlice("Laptop");
        service.changePassword("alice.chen@mail.mcgill.ca", "Racket!2026", "Smash!2027");

        service.logOut("Laptop");

        assertFalse(service.isLoggedIn(ALICE));
    }

    @Test
    void loggingOutOneOfTwoAccountsWithTheSameNameKeepsTheOthersSession() {
        Account otherAlice = service.createAccount("Alice Chen", "alice.chen2@mail.mcgill.ca", "Other!2026");
        logInAlice("Laptop");
        service.logIn("alice.chen2@mail.mcgill.ca", "Other!2026", "Phone");

        service.logOut("Laptop");

        assertTrue(service.getActiveSession("Laptop").isEmpty());
        assertSame(otherAlice, service.getActiveSession("Phone").orElseThrow().account());
    }
}
