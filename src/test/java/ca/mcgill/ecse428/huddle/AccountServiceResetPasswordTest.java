package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID011: resetting a password with a valid link (TID025) and rejecting expired or used links (TID026). */
class AccountServiceResetPasswordTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 17, 0);

    private AccountService service;
    private Account alice;
    private Account frank;
    private PasswordResetLink l1;
    private PasswordResetLink l2;
    private PasswordResetLink l3;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        alice = new Account(new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
        frank = new Account(new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026", Role.FACILITY_ADMINISTRATOR);
        service.addAccount(alice);
        service.addAccount(frank);
        service.setCurrentDateTime(NOW);
        l1 = new PasswordResetLink("L1", alice, LocalDateTime.of(2026, 11, 9, 18, 0));
        l2 = new PasswordResetLink("L2", alice, LocalDateTime.of(2026, 11, 9, 16, 0));
        l3 = new PasswordResetLink("L3", frank, LocalDateTime.of(2026, 11, 9, 18, 0));
        l3.markUsed();
        service.addResetLink(l1);
        service.addResetLink(l2);
        service.addResetLink(l3);
    }

    @Test
    void validLinkSetsNewPasswordAndMarksItUsed() {
        Account returned = service.resetPassword("L1", "Shuttle!2026");
        assertSame(alice, returned);
        assertSame(alice, service.logIn("alice.chen@mail.mcgill.ca", "Shuttle!2026").account());
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026"));
        assertEquals("Invalid email address or password", ex.getMessage());
        assertTrue(l1.isUsed());
    }

    @Test
    void passwordResetMessageIsTheExpectedWording() {
        assertEquals("Your password has been reset", AccountService.PASSWORD_RESET_MESSAGE);
    }

    @Test
    void expiredLinkIsRejectedAndNothingChanges() {
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> service.resetPassword("L2", "Shuttle!2026"));
        assertEquals("This password reset link is invalid or has expired", ex.getMessage());
        assertFalse(l2.isUsed());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void alreadyUsedLinkIsRejectedAndNothingChanges() {
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> service.resetPassword("L3", "Shuttle!2026"));
        assertEquals("This password reset link is invalid or has expired", ex.getMessage());
        assertTrue(frank.passwordMatches("Admin!2026"));
    }

    @Test
    void unknownTokenIsRejected() {
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> service.resetPassword("L9", "Shuttle!2026"));
        assertEquals("This password reset link is invalid or has expired", ex.getMessage());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void nullTokenIsRejected() {
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> service.resetPassword(null, "Shuttle!2026"));
        assertEquals("This password reset link is invalid or has expired", ex.getMessage());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void aLinkCannotBeUsedTwice() {
        service.resetPassword("L1", "Shuttle!2026");
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> service.resetPassword("L1", "Second!2026"));
        assertEquals("This password reset link is invalid or has expired", ex.getMessage());
        assertTrue(alice.passwordMatches("Shuttle!2026"));
    }

    @Test
    void aLinkExpiringExactlyAtTheCurrentTimeCountsAsExpired() {
        service.setCurrentDateTime(LocalDateTime.of(2026, 11, 9, 18, 0));
        InvalidAccountException ex = assertThrows(InvalidAccountException.class,
                () -> service.resetPassword("L1", "Shuttle!2026"));
        assertEquals("This password reset link is invalid or has expired", ex.getMessage());
        assertFalse(l1.isUsed());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void aValidLinkBecomesExpiredWhenTheClockMovesPastItsExpiry() {
        service.setCurrentDateTime(LocalDateTime.of(2026, 11, 9, 18, 0));
        assertThrows(InvalidAccountException.class, () -> service.resetPassword("L1", "Shuttle!2026"));
    }

    @Test
    void nullNewPasswordIsRejectedAndNothingChanges() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> service.resetPassword("L1", null));
        assertEquals("Password is required", ex.getMessage());
        assertFalse(l1.isUsed());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void emptyNewPasswordIsRejectedAndNothingChanges() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("L1", ""));
        assertEquals("Password must not be empty", ex.getMessage());
        assertFalse(l1.isUsed());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void changePasswordReplacesThePassword() {
        alice.changePassword("Shuttle!2026");
        assertTrue(alice.passwordMatches("Shuttle!2026"));
        assertFalse(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void changePasswordRejectsNull() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> alice.changePassword(null));
        assertEquals("Password is required", ex.getMessage());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void changePasswordRejectsEmpty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> alice.changePassword(""));
        assertEquals("Password must not be empty", ex.getMessage());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void changePasswordDoesNotStripThePassword() {
        alice.changePassword(" Shuttle!2026 ");
        assertTrue(alice.passwordMatches(" Shuttle!2026 "));
        assertFalse(alice.passwordMatches("Shuttle!2026"));
    }

    @Test
    void invalidResetLinkMessageIsTheExpectedWording() {
        assertEquals("This password reset link is invalid or has expired",
                InvalidAccountException.INVALID_RESET_LINK_MESSAGE);
    }
}
