package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ID013: changing the name of an account (TID041). */
class AccountServiceChangeNameTest {

    private AccountService service;
    private Account alice;

    @BeforeEach
    void setUp() {
        service = new AccountService();
        alice = new Account(new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
        service.addAccount(alice);
        service.addAccount(new Account(new User("Frank Ho"), "frank.ho@mcgill.ca", "Admin!2026",
                Role.FACILITY_ADMINISTRATOR));
    }

    @Test
    void nameIsChangedAndTheRestOfTheAccountIsKept() {
        Account changed = service.changeName(new User("Alice Chen"), "Alice Chen-Roy");

        assertSame(alice, changed);
        assertEquals("Alice Chen-Roy", service.findByEmail("alice.chen@mail.mcgill.ca").orElseThrow().getName());
        assertEquals(new User("Alice Chen-Roy"), alice.getUser());
        assertEquals("alice.chen@mail.mcgill.ca", alice.getEmail());
        assertEquals(Role.STUDENT, alice.getRole());
        assertTrue(alice.passwordMatches("Racket!2026"));
    }

    @Test
    void newNameIsStripped() {
        service.changeName(new User("Alice Chen"), "  Alice Chen-Roy ");
        assertEquals("Alice Chen-Roy", alice.getName());
    }

    @Test
    void accountIsFoundByItsNewNameAfterwards() {
        service.changeName(new User("Alice Chen"), "Alice Chen-Roy");

        assertSame(alice, service.findByUser(new User("Alice Chen-Roy")).orElseThrow());
        assertTrue(service.findByUser(new User("Alice Chen")).isEmpty());
    }

    @Test
    void activeSessionShowsTheNewName() {
        service.logIn("alice.chen@mail.mcgill.ca", "Racket!2026");

        service.changeName(new User("Alice Chen"), "Alice Chen-Roy");

        assertEquals(new User("Alice Chen-Roy"), service.getActiveSession().orElseThrow().user());
        assertTrue(service.isLoggedIn(new User("Alice Chen-Roy")));
    }

    @Test
    void blankNameIsRejectedAndTheNameIsKept() {
        assertThrows(IllegalArgumentException.class, () -> service.changeName(new User("Alice Chen"), "  "));
        assertThrows(NullPointerException.class, () -> service.changeName(new User("Alice Chen"), null));
        assertEquals("Alice Chen", alice.getName());
    }

    @Test
    void userWithoutAnAccountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.changeName(new User("Dan Lee"), "Dan Li"));
    }

    @Test
    void confirmationMessageIsTheOneTheFeatureFileExpects() {
        assertEquals("Profile updated", AccountService.PROFILE_UPDATED_MESSAGE);
    }
}
