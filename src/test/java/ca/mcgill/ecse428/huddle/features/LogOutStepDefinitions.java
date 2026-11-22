package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.Account;
import ca.mcgill.ecse428.huddle.AccountService;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/** Steps for the ID012 logout scenarios. */
public class LogOutStepDefinitions {
    private final HuddleWorld world;

    public LogOutStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    @Given("{string} is logged in to Huddle on device {string}")
    public void loggedInOnDevice(String name, String device) {
        Account account = world.accountService.findByUser(new User(name))
                .orElseThrow(() -> new AssertionError("No account for " + name));
        String password = world.accountPasswordsByName.get(name);
        if (password == null) throw new AssertionError("No credentials for " + name);
        world.accountService.logIn(account.getEmail(), password, device);
    }

    @When("{string} logs out")
    public void logsOut(String name) {
        logsOutOnDevice(name, AccountService.DEFAULT_DEVICE);
    }

    @When("{string} logs out on device {string}")
    public void logsOutOnDevice(String name, String device) {
        assertTrue(world.accountService.isLoggedIn(new User(name), device), "Expected a session for " + name);
        try {
            world.accountService.logOut(device);
            world.messages.add(AccountService.LOGGED_OUT_MESSAGE);
        } catch (RuntimeException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("{string} does not have an active session in Huddle")
    public void noSession(String name) {
        assertFalse(world.accountService.isLoggedIn(new User(name)));
    }

    @Then("{string} does not have an active session in Huddle on device {string}")
    public void noSessionOnDevice(String name, String device) {
        assertFalse(world.accountService.isLoggedIn(new User(name), device));
    }

    @Then("{string} has an active session in Huddle on device {string}")
    public void hasSessionOnDevice(String name, String device) {
        assertTrue(world.accountService.isLoggedIn(new User(name), device));
    }

    @When("a user attempts to log out")
    public void attemptsLogoutWithoutSession() {
        try {
            world.accountService.logOut(AccountService.DEFAULT_DEVICE);
        } catch (RuntimeException e) {
            world.errorMessage = e.getMessage();
        }
    }
}
