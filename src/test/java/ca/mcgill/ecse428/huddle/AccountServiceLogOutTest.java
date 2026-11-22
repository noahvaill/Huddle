package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for logging out on one device, AccountService.logOut (ID012, TID033). */
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

    @Test
    void loggedOutMessageMatchesTheFeatureFile() {
        assertEquals("You have been logged out", AccountService.LOGGED_OUT_MESSAGE);
    }
}
