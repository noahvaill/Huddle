package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID011: AccountService keeps the reset links (TID023) and has a settable current date and time (TID027). */
class AccountServiceResetLinkTest {

    private static final Account ALICE = new Account(
            new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
    private static final LocalDateTime NOON = LocalDateTime.of(2026, 11, 20, 12, 0);

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    @Test
    void addedLinkIsFoundByToken() {
        PasswordResetLink link = new PasswordResetLink("L1", ALICE, NOON);
        service.addResetLink(link);
        assertSame(link, service.findResetLink("L1").orElseThrow());
        assertTrue(service.findResetLink("L2").isEmpty());
        assertTrue(service.findResetLink(null).isEmpty());
    }

    @Test
    void twoLinksWithTheSameTokenAreRejected() {
        service.addResetLink(new PasswordResetLink("L1", ALICE, NOON));
        assertThrows(IllegalArgumentException.class,
                () -> service.addResetLink(new PasswordResetLink("L1", ALICE, NOON.plusHours(1))));
    }

    @Test
    void currentDateTimeCanBeSetForTests() {
        service.setCurrentDateTime(NOON);
        assertEquals(NOON, service.currentDateTime());
    }

    @Test
    void currentDateTimeIsTheRealTimeByDefault() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime now = service.currentDateTime();
        assertTrue(!now.isBefore(before) && !now.isAfter(LocalDateTime.now()));
    }
}
