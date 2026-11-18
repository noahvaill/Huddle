package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.AccountService;
import ca.mcgill.ecse428.huddle.InvalidAccountException;
import ca.mcgill.ecse428.huddle.Role;
import ca.mcgill.ecse428.huddle.Session;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for ID002 Log In (TID011).
 * The accounts table of the Background is in CreateAccountStepDefinitions (ID001, TID004) and the message checks
 * are in CommonStepDefinitions.
 */
public class LogInStepDefinitions {

    private final HuddleWorld world;

    public LogInStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    /** The name only says who is at the keyboard: logging in uses the email and password (e.g. Dan Lee has no account). */
    @When("{string} logs in with email {string} and password {string}")
    public void logs_in_with_email_and_password(String userName, String email, String password) {
        try {
            Session session = world.accountService.logIn(email, password);
            world.messages.add(AccountService.loggedInMessage(session.account()));
        } catch (InvalidAccountException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("{string} has an active session in Huddle as a {string}")
    public void has_an_active_session_in_huddle_as_a(String userName, String roleName) {
        Session session = world.accountService.getActiveSession()
                .orElseThrow(() -> new AssertionError("No active session, error message: " + world.errorMessage));
        assertEquals(new User(userName), session.user());
        assertEquals(Role.fromName(roleName), session.role());
    }

    @Then("no active session exists in Huddle")
    public void no_active_session_exists_in_huddle() {
        assertTrue(world.accountService.getActiveSession().isEmpty(), "Expected no active session");
    }
}