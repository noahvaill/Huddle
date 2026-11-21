package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID011: requesting a password reset link, with the same message for unknown emails (TID024). */
class AccountServiceRequestResetLinkTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 17, 0);

    private AccountService service;
    private Account alice;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        alice = new Account(new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
        service.addAccount(alice);
        service.setCurrentDateTime(NOW);
    }

    @Test
    void knownEmailGetsAnUnusedLinkThatExpiresAfterTheValidityPeriod() {
        PasswordResetLink link = service.requestResetLink("alice.chen@mail.mcgill.ca").orElseThrow();

        assertSame(alice, link.getAccount());
        assertEquals(NOW.plus(AccountService.RESET_LINK_VALIDITY), link.getExpiresAt());
        assertFalse(link.isUsed());
        assertFalse(link.isExpiredAt(NOW));
        assertSame(link, service.findResetLink(link.getToken()).orElseThrow());
    }

    @Test
    void emailIsMatchedInAnyCaseAndWithSurroundingSpaces() {
        assertSame(alice, service.requestResetLink("  Alice.Chen@Mail.McGill.ca ").orElseThrow().getAccount());
    }

    @Test
    void unknownOrMissingEmailCreatesNoLink() {
        assertTrue(service.requestResetLink("dan.lee@mail.mcgill.ca").isEmpty());
        assertTrue(service.requestResetLink(null).isEmpty());
    }

    @Test
    void eachRequestGetsANewLinkWithItsOwnToken() {
        PasswordResetLink first = service.requestResetLink("alice.chen@mail.mcgill.ca").orElseThrow();
        PasswordResetLink second = service.requestResetLink("alice.chen@mail.mcgill.ca").orElseThrow();

        assertNotEquals(first.getToken(), second.getToken());
        assertSame(first, service.findResetLink(first.getToken()).orElseThrow());
    }

    @Test
    void requestedLinkCanResetThePasswordOnce() {
        String token = service.requestResetLink("alice.chen@mail.mcgill.ca").orElseThrow().getToken();

        service.resetPassword(token, "NewPass!2026");

        assertTrue(alice.passwordMatches("NewPass!2026"));
        assertTrue(service.findResetLink(token).orElseThrow().isUsed());
    }

    @Test
    void messageIsTheSameWhetherOrNotTheEmailHasAnAccount() {
        Optional<PasswordResetLink> known = service.requestResetLink("alice.chen@mail.mcgill.ca");
        Optional<PasswordResetLink> unknown = service.requestResetLink("dan.lee@mail.mcgill.ca");

        assertTrue(known.isPresent());
        assertTrue(unknown.isEmpty());
        assertEquals("If an account exists for this email address, a password reset link has been sent",
                AccountService.RESET_LINK_SENT_MESSAGE);
    }
}
