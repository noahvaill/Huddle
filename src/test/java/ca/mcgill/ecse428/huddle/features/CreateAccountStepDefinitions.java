package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.Account;
import ca.mcgill.ecse428.huddle.AccountService;
import ca.mcgill.ecse428.huddle.InvalidAccountException;
import ca.mcgill.ecse428.huddle.Role;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;

/**
 * Step definitions for ID001 Create Account (TID004), including the scenario outline for non-McGill emails.
 * The confirmation and error message steps are shared: see CommonStepDefinitions.
 */
public class CreateAccountStepDefinitions {

    private final HuddleWorld world;

    public CreateAccountStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    /** Adds existing accounts as they are, without the McGill email rules, e.g. Alice in the Background. */
    @Given("the following accounts exist in Huddle:")
    public void the_following_accounts_exist_in_huddle(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            world.accountPasswordsByName.put(row.get("name"), row.get("password"));
            world.accountService.addAccount(new Account(
                    new User(row.get("name")),
                    row.get("email"),
                    row.get("password"),
                    Role.fromName(row.get("role"))));
        }
    }

    @When("{string} requests an account with email {string} and password {string}")
    public void requests_an_account_with_email_and_password(String name, String email, String password) {
        try {
            world.accountService.createAccount(name, email, password);
            world.messages.add(AccountService.ACCOUNT_CREATED_MESSAGE);
        } catch (InvalidAccountException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("an account exists for {string} with name {string}")
    public void an_account_exists_for_with_name(String email, String name) {
        Account account = world.accountService.findByEmail(email)
                .orElseThrow(() -> new AssertionError("No account for " + email));
        assertEquals(name, account.getName());
    }

    @Then("no account exists for {string}")
    public void no_account_exists_for(String email) {
        assertTrue(world.accountService.findByEmail(email).isEmpty(), "Expected no account for " + email);
    }

    @Then("only one account exists for {string}")
    public void only_one_account_exists_for(String email) {
        long count = world.accountService.getAccounts().stream()
                .filter(a -> a.getEmail().equals(Account.normalizeEmail(email)))
                .count();
        assertEquals(1, count);
    }
}
