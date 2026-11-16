package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

    private static final Account ALICE = new Account(
            new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
    private static final Account FRANK = new Account(
            new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026", Role.FACILITY_ADMINISTRATOR);

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    @Test
    void addedAccountIsFoundByEmail() {
        service.addAccount(ALICE);
        assertSame(ALICE, service.findByEmail("alice.chen@mail.mcgill.ca").orElseThrow());
    }

    @Test
    void emailLookupIgnoresCaseAndSurroundingSpaces() {
        service.addAccount(ALICE);
        assertSame(ALICE, service.findByEmail("  ALICE.Chen@Mail.McGill.ca ").orElseThrow());
    }

    @Test
    void unknownNullOrBlankEmailFindsNothing() {
        service.addAccount(ALICE);
        assertTrue(service.findByEmail("dan.lee@mail.mcgill.ca").isEmpty());
        assertTrue(service.findByEmail(null).isEmpty());
        assertTrue(service.findByEmail("   ").isEmpty());
    }

    @Test
    void secondAccountForTheSameEmailIsRejectedInAnyCase() {
        service.addAccount(ALICE);
        Account copy = new Account(new User("Alice C"), "Alice.Chen@mail.mcgill.ca", "Other!2026", Role.STUDENT);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.addAccount(copy));
        assertEquals("An account already exists for alice.chen@mail.mcgill.ca", e.getMessage());
        assertEquals(List.of(ALICE), service.getAccounts());
        assertSame(ALICE, service.findByEmail("alice.chen@mail.mcgill.ca").orElseThrow());
    }

    @Test
    void nullAccountIsRejected() {
        assertThrows(NullPointerException.class, () -> service.addAccount(null));
    }

    @Test
    void accountIsFoundByItsUser() {
        service.addAccount(ALICE);
        service.addAccount(FRANK);
        assertSame(FRANK, service.findByUser(new User("Frank Ho")).orElseThrow());
        assertTrue(service.findByUser(new User("Dan Lee")).isEmpty());
    }

    @Test
    void accountsAreListedInTheOrderAddedAndTheListCannotBeChanged() {
        service.addAccount(FRANK);
        service.addAccount(ALICE);
        List<Account> accounts = service.getAccounts();
        assertEquals(List.of(FRANK, ALICE), accounts);
        assertThrows(UnsupportedOperationException.class, () -> accounts.add(ALICE));
    }
}
