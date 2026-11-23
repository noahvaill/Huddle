package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.mcgill.ecse428.huddle.Account;
import ca.mcgill.ecse428.huddle.AccountService;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Steps for the ID013 edit profile scenarios (TID044). Logging in with the new or old password reuses the
 * ID011 steps ("can log in" / "cannot log in").
 */
public class EditProfileStepDefinitions {
    private final HuddleWorld world;

    public EditProfileStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    @When("{string} changes her/his/their name to {string}")
    public void changes_name_to(String userName, String newName) {
        try {
            world.accountService.changeName(new User(userName), newName);
            world.messages.add(AccountService.PROFILE_UPDATED_MESSAGE);
        } catch (RuntimeException e) {
            world.errorMessage = e.getMessage();
        }
    }

    /** The account is found by name here, then changed by email, as AccountService.changePassword expects. */
    @When("{string} changes her/his/their password from {string} to {string}")
    public void changes_password_from_to(String userName, String currentPassword, String newPassword) {
        Account account = world.accountService.findByUser(new User(userName))
                .orElseThrow(() -> new AssertionError("No account for " + userName));
        try {
            world.accountService.changePassword(account.getEmail(), currentPassword, newPassword);
            world.messages.add(AccountService.PROFILE_UPDATED_MESSAGE);
        } catch (RuntimeException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("the account for {string} has name {string}")
    public void the_account_for_has_name(String email, String expectedName) {
        Account account = world.accountService.findByEmail(email)
                .orElseThrow(() -> new AssertionError("No account for " + email));
        assertEquals(expectedName, account.getName());
    }
}
