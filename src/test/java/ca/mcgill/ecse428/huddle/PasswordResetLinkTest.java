package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** ID011: a password reset link expires at a given time and can be used once (TID023). */
class PasswordResetLinkTest {

    private static final Account ALICE = new Account(
            new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 11, 20, 18, 0);

    private static PasswordResetLink link() {
        return new PasswordResetLink("L1", ALICE, EXPIRES_AT);
    }

    @Test
    void newLinkIsUnusedAndKeepsItsFields() {
        PasswordResetLink link = link();
        assertEquals("L1", link.getToken());
        assertSame(ALICE, link.getAccount());
        assertEquals(EXPIRES_AT, link.getExpiresAt());
        assertFalse(link.isUsed());
    }

    @Test
    void linkIsExpiredFromItsExpiryTimeOn() {
        PasswordResetLink link = link();
        assertFalse(link.isExpiredAt(EXPIRES_AT.minusMinutes(1)));
        assertTrue(link.isExpiredAt(EXPIRES_AT));
        assertTrue(link.isExpiredAt(EXPIRES_AT.plusDays(1)));
    }

    @Test
    void linkCanBeMarkedUsedOnlyOnce() {
        PasswordResetLink link = link();
        link.markUsed();
        assertTrue(link.isUsed());
        assertEquals("Reset link has already been used",
                assertThrows(IllegalStateException.class, link::markUsed).getMessage());
    }

    @Test
    void missingOrBlankFieldsAreRejected() {
        assertEquals("Token must not be blank", assertThrows(IllegalArgumentException.class,
                () -> new PasswordResetLink("  ", ALICE, EXPIRES_AT)).getMessage());
        assertThrows(NullPointerException.class, () -> new PasswordResetLink(null, ALICE, EXPIRES_AT));
        assertThrows(NullPointerException.class, () -> new PasswordResetLink("L1", null, EXPIRES_AT));
        assertThrows(NullPointerException.class, () -> new PasswordResetLink("L1", ALICE, null));
    }
}
